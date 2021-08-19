package com.kpstv.yts.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kpstv.common_moviesy.extensions.applyBottomInsets
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.colorFrom
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
import com.kpstv.yts.ui.helpers.ThemeHelper
import com.kpstv.yts.ui.helpers.ThemeHelper.registerForThemeChange
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import com.kpstv.yts.vpn.VPNViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import kotlin.reflect.KClass

interface MainFragmentDrawerCallbacks {
    fun openDrawer()
    fun isDrawerOpen(): Boolean
}

interface MainFragmentContinueWatchCallbacks {
    fun selectMovie(movieId: Int)
}

@AndroidEntryPoint
class MainFragment : ValueFragment(R.layout.fragment_main), FragmentNavigator.Transmitter, MainFragmentDrawerCallbacks, MainFragmentContinueWatchCallbacks {
    private val binding by viewBinding(FragmentMainBinding::bind)
    private val navViewModel by activityViewModels<StartViewModel>()
    private val vpnViewModel by activityViewModels<VPNViewModel>()
    private val viewModel by viewModels<MainViewModel>()
    private val navigations by lazy {
        Navigations(requireContext())
    }
    private lateinit var navigator: FragmentNavigator
    @Inject lateinit var updateUtils: UpdateUtils

    private var currentBottomNavId: Int = 0
    private var isUpdateChecked = false
    private var isDozeChecked = false

    private lateinit var bottomController: BottomNavigationController

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerForThemeChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(binding.fragmentContainer)

        bottomController = navigator.install(object : FragmentNavigator.BottomNavigation() {
            override val bottomNavigationViewId: Int = R.id.bottom_nav
            override val bottomNavigationFragments: Map<Int, KClass<out Fragment>> = mapOf(
                R.id.homeFragment to HomeFragment::class,
                R.id.watchFragment to WatchlistFragment::class,
                R.id.libraryFragment to LibraryFragment::class
            )
            override fun onBottomNavigationSelectionChanged(selectedId: Int) {
                currentBottomNavId = selectedId
            }

            override val selectedFragmentId: Int
                get() {
                    if (hasKeyArgs<Args>() && getKeyArgs<Args>().moveToLibrary) {
                        return R.id.libraryFragment
                    }
                    return super.selectedFragmentId
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

        observeVPNColorChange()

        ChangelogHelper(requireContext(), childFragmentManager).show()

        if (savedInstanceState == null) {
            if (hasKeyArgs<Args>()) {
                manageArgumentsAndConsume()
            }
            if (viewModel.uiState.mainFragmentState.isDrawerOpen == true) openDrawer()
        }
    }

    override fun onStart() {
        super.onStart()
        // TODO: Attempted to fix: This is fucking up when Moviesy's notification are clicked after the process death (precisely viewLifecycleOwner access in checkForAutoPurchase method).
        if (!isUpdateChecked) {
            isUpdateChecked = true
            updateUtils.check(
                onUpdateAvailable = {
                    updateUtils.showUpdateDialog(requireContext()) {
                        updateUtils.processUpdate(it)
                        Toasty.info(requireContext(), getString(R.string.update_download_text)).show()
                    }
                },
                onUpdateNotFound = {
                    if (isAdded && !isRemoving) {
                        checkForAutoPurchase()
                    }
                },
                onVersionDeprecated = {
                    AppUtils.doOnVersionDeprecated(requireContext())
                },
                onError = {
                    Toasty.error(requireContext(), "Update check failed: ${it.message}").show()
                }
            )
        }
        if (!isDozeChecked) {
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

    override fun selectMovie(movieId: Int) {
        bottomController.select(R.id.libraryFragment, LibraryFragment.Args(movieId = movieId))
    }

    override val forceBackPress: Boolean
        get() = (currentBottomNavId != R.id.homeFragment) or isDrawerOpen()

    override fun onBackPressed(): Boolean {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> closeDrawer()
            currentBottomNavId != R.id.homeFragment -> bottomController.select(R.id.homeFragment)
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
        clearArgs<Args>()
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
            navigations.updateNotification(NAV_DOWNLOAD_QUEUE, it.size)
        })
    }

    private fun observeVPNColorChange() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vpnViewModel.showHover.collect { state ->
                with(binding.navigationLayout.ivVpn) {
                    if (state) {
                        setColorFilter(colorFrom(R.color.google_cast))
                    } else {
                        clearColorFilter()
                    }
                }
            }
        }
    }

    private fun setNavigationDrawerItemClicks() {
        binding.navigationLayout.ivVpn.setOnClickListener {
            vpnViewModel.toggleHover()
            closeDrawer()
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
                    animation = AnimationDefinition.Fade,
                    remember = true
                )
            }
        }
    }

    private fun setPremiumButtonClicked() {
        binding.navigationLayout.customDrawerPremium.root.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            if (!AppInterface.IS_PREMIUM_UNLOCKED) {
                PremiumHelper.openPurchaseFragment(childFragmentManager)
            } else {
                PremiumHelper.showPremiumAlreadyPurchasedDialog(requireContext())
            }
        }
    }

    private fun checkForAutoPurchase() {
        PremiumHelper.scanForAutoPurchase(requireContext(), lifecycleOwner = viewLifecycleOwner,
            onPremiumActivated = {
                PremiumHelper.showPremiumActivatedDialog(requireContext())
            },
            onNoPremiumFound = {
                PremiumHelper.showPurchaseInfo(requireContext(), childFragmentManager)
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
            (requireActivity() as? LibraryFragment.Callbacks)?.getCastHelper()?.unInit()
        }
        super.onDestroy()
    }

    @Parcelize
    data class Args(
        val moveToLibrary: Boolean =  false,
        val moveToDetail: DetailFragment.Args? = null,
        val forceUpdateCheck: Boolean = false
    ) : BaseArgs(), Parcelable

    interface LibraryInterface {
        fun openDownload(movieId: Int)
    }

    companion object {
        const val NAV_DOWNLOAD_QUEUE = "nav_download_queue"
        const val NAV_SETTINGS = "nav_settings"
    }
}