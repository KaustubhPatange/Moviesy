package com.kpstv.yts.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kpstv.common_moviesy.extensions.applyBottomInsets
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.*
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.databinding.FragmentMainBinding
import com.kpstv.yts.extensions.NavigationModel
import com.kpstv.yts.extensions.NavigationModels
import com.kpstv.yts.extensions.Navigations
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.UpdateUtils
import com.kpstv.yts.ui.activities.DownloadActivity
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.helpers.BatteryOptimizationHelper
import com.kpstv.yts.ui.helpers.ChangelogHelper
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.helpers.ThemeHelper.registerForThemeChange
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

interface MainFragmentDrawerCallbacks {
    fun openDrawer()
    fun isDrawerOpen(): Boolean
}

@AndroidEntryPoint
class MainFragment : KeyedFragment(R.layout.fragment_main), MainFragmentDrawerCallbacks {
    private val binding by viewBinding(FragmentMainBinding::bind)
    private val navViewModel by activityViewModels<StartViewModel>()
    private val viewModel by viewModels<MainViewModel>()
    private val navigations by lazy {
        Navigations(requireContext())
    }
    private lateinit var navigator: Navigator
    @Inject lateinit var updateUtils: UpdateUtils

    private var currentBottomNavId: Int = 0
    private var isUpdateChecked = false
    private var isDozeChecked = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerForThemeChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = Navigator(childFragmentManager, binding.fragmentContainer)
        navigator.install(this, object : Navigator.BottomNavigation() {
            override val bottomNavigationViewId: Int = R.id.bottom_nav
            override val bottomNavigationFragments: Map<Int, FragClazz> = mapOf(
                R.id.homeFragment to HomeFragment2::class,
                R.id.watchFragment to WatchlistFragment2::class,
                R.id.libraryFragment to LibraryFragment2::class
            )

            override fun onBottomNavigationSelectionChanged(selectedId: Int) {
                currentBottomNavId = selectedId
            }

            override val selectedBottomNavigationId: Int
                get() {
                    if (hasKeyArgs()) {
                        if (getKeyArgs<Args>().moveToLibrary) {
                            return R.id.libraryFragment
                        }
                    }
                    return super.selectedBottomNavigationId
                }
        })

        binding.apply {
            root.applyTopInsets(navigationLayout.navRootLayout)
            root.applyBottomInsets(navigationLayout.navRootLayout)
        }

        animateBottomNavUp()
        setNavigationDrawer()
        setNavigationDrawerItemClicks()
        setPremiumButtonClicked()

        ChangelogHelper(requireContext(), childFragmentManager).show()

        if (savedInstanceState == null) {
            if (hasKeyArgs()) {
                manageArgumentsAndConsume()
            }
            if (viewModel.uiState.mainFragmentState.isDrawerOpen == true) openDrawer()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isUpdateChecked && isAdded) {
            isUpdateChecked = true
            updateUtils.check(
                onUpdateAvailable = {
                    updateUtils.showUpdateDialog(requireContext()) {
                        updateUtils.processUpdate(it)
                        Toasty.info(requireContext(), getString(R.string.update_download_text)).show()
                    }
                },
                onUpdateNotFound = {
                    checkForAutoPurchase()
                },
                onVersionDeprecated = {
                    AppUtils.doOnVersionDeprecated(requireContext())
                },
                onError = {
                    Toasty.error(requireContext(), "Failed: ${it.message}").show()
                }
            )
        }
        if (!isDozeChecked && isAdded) {
            isDozeChecked = true
            BatteryOptimizationHelper.askNoBatteryOptimization(requireContext())
        }
    }

    override fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun isDrawerOpen(): Boolean = binding.drawerLayout.isDrawerOpen(GravityCompat.START)

    override val forceBackPress: Boolean
        get() = (currentBottomNavId != R.id.homeFragment) or isDrawerOpen()

    override fun onBackPressed(): Boolean {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> closeDrawer()
            currentBottomNavId != R.id.homeFragment -> binding.bottomNav.selectedItemId = R.id.homeFragment
            else -> return super.onBackPressed()
        }
        return true
    }

    private fun manageArgumentsAndConsume() {
        val args = getKeyArgs<Args>()
        if (args.forceUpdateCheck) {
            isUpdateChecked = false
        }
        requireView().doOnPreDraw {
            if (args.moveToDetail != null) {
                val a = args.moveToDetail
                navViewModel.goToDetail(a.ytsId, a.tmDbId, a.movieUrl)
            }
        }

        clearArgs()
    }

    private fun setNavigationDrawer() {
        val models = NavigationModels().apply {
            add(
                NavigationModel(
                    tag = NAV_DOWNLOAD_QUEUE,
                    title = getString(R.string.nav_download),
                    drawableRes = R.drawable.ic_queue
                )
            )
            add(
                NavigationModel(
                    tag = NAV_SETTINGS,
                    title = getString(R.string.settings),
                    drawableRes = R.drawable.ic_settings
                )
            )
        }
        navigations.setUp(binding.navigationLayout.navRecyclerView, models) { navigationModel, _ ->
            navigateTo(navigationModel.tag)
        }
        viewModel.pauseMovieJob.observe(viewLifecycleOwner, {
            navigations.updateNotification(NAV_DOWNLOAD_QUEUE, it.size) // TODO: Update this & make it for childFragmentManager
        })
    }

    private fun setNavigationDrawerItemClicks() {
        binding.navigationLayout.ivReport.setOnClickListener {
            AppUtils.launchUrl(requireContext(), "${getString(R.string.app_github)}/issues", AppInterface.IS_DARK_THEME)
        }
        binding.navigationLayout.ivShare.setOnClickListener {
            AppUtils.shareApp(requireActivity())
        }
    }

    private fun navigateTo(tag: String) {
        closeDrawer()
        when (tag) {
            NAV_DOWNLOAD_QUEUE -> {
                val downloadIntent = Intent(requireContext(), DownloadActivity::class.java)
                startActivity(downloadIntent)
            }
            NAV_SETTINGS -> {
                navViewModel.navigateTo(
                    StartActivity.Screen.SETTING,
                    transition = Navigator.TransitionType.FADE,
                    addToBackStack = true
                )
            }
        }
    }

    private fun setPremiumButtonClicked() {
        binding.navigationLayout.customDrawerPremium.root.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            if (!AppInterface.IS_PREMIUM_UNLOCKED) {
                PremiumHelper.openPurchaseFragment(requireActivity()) // TODO: Update this & make for childFragmentManager
            } else {
                PremiumHelper.showPremiumAlreadyPurchasedDialog(requireContext())
            }
        }
    }

    private fun checkForAutoPurchase() {
        PremiumHelper.scanForAutoPurchase(requireActivity(),
            onPremiumActivated = {
                PremiumHelper.showPremiumActivatedDialog(requireContext())
            },
            onNoPremiumFound = {
                PremiumHelper.showPurchaseInfo(requireActivity())
            }
        )
    }

    private fun animateBottomNavUp() {
        binding.bottomNav.translationY = 500f
        binding.root.doOnPreDraw {
            if (isVisible) {
                binding.bottomNav.animate().translationY(0f).setStartDelay(70).setDuration(100).start()
            }
        }
    }

    private fun animateBottomNavDown() {
        binding.bottomNav.animate().translationY(500f).start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.uiState.mainFragmentState.isDrawerOpen =
            view?.findViewById<DrawerLayout>(R.id.drawer_layout)?.isDrawerOpen(GravityCompat.START)
    }

    override fun onDestroy() {
        if (CastHelper.isCastingSupported(requireContext())) {
            (requireActivity() as? LibraryFragment2.Callbacks)?.getCastHelper()?.unInit()
        }
        super.onDestroy()
    }

    @Parcelize
    data class Args(
        val moveToLibrary: Boolean =  false,
        val moveToDetail: DetailFragment.Args? = null,
        val forceUpdateCheck: Boolean = false
    ) : BaseArgs(), Parcelable

    companion object {
        const val NAV_DOWNLOAD_QUEUE = "nav_download_queue"
        const val NAV_SETTINGS = "nav_settings"
    }
}