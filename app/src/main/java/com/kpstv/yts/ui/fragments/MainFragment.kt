package com.kpstv.yts.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.navigation.BaseArgs
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ActivityMainBinding
import com.kpstv.yts.extensions.NavigationModel
import com.kpstv.yts.extensions.NavigationModels
import com.kpstv.yts.extensions.Navigations
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.activities.DownloadActivity
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.helpers.ChangelogHelper
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.helpers.ThemeHelper.updateTheme
import com.kpstv.navigation.KeyedFragment
import com.kpstv.navigation.Navigator
import com.kpstv.yts.ui.helpers.ThemeHelper.registerForThemeChange
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize

@AndroidEntryPoint
class MainFragment : KeyedFragment(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val navViewModel by activityViewModels<StartViewModel>()
    private val viewModel by viewModels<MainViewModel>()
    private val navigations by lazy {
        Navigations(requireContext())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerForThemeChange(parentFragmentManager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            root.applyTopInsets(navigationLayout.navRootLayout)
            root.applyBottomInsets(navigationLayout.navRootLayout)
        }

        animateBottomNavUp()
        setNavigationDrawer()
        setNavigationDrawerItemClicks()
        setPremiumButtonClicked()
        if (hasKeyArgs()) {
            manageArguments()
        }

        ChangelogHelper(requireActivity()).show() // TODO: Update this & make it for childFragmentManager
    }

    private fun manageArguments() {
        val args = getKeyArgs<Args>()
        if (args.moveToLibrary) {
            // TODO: Move to Library
        }
    }

    private fun setNavigationDrawer() {
        val models = NavigationModels().apply {
            add(
                NavigationModel(
                    tag = MainActivity.NAV_DOWNLOAD_QUEUE,
                    title = getString(R.string.nav_download),
                    drawableRes = R.drawable.ic_queue
                )
            )
            add(
                NavigationModel(
                    tag = MainActivity.NAV_SETTINGS,
                    title = getString(R.string.settings),
                    drawableRes = R.drawable.ic_settings
                )
            )
        }
        navigations.setUp(binding.navigationLayout.navRecyclerView, models) { navigationModel, _ ->
            navigateTo(navigationModel.tag)
        }
        viewModel.pauseMovieJob.observe(viewLifecycleOwner, {
            navigations.updateNotification(MainActivity.NAV_DOWNLOAD_QUEUE, it.size) // TODO: Update this & make it for childFragmentManager
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
        binding.root.closeDrawer(GravityCompat.START)
        when (tag) {
            MainActivity.NAV_DOWNLOAD_QUEUE -> {
                val downloadIntent = Intent(requireContext(), DownloadActivity::class.java)
                startActivity(downloadIntent)
            }
            MainActivity.NAV_SETTINGS -> {
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

    private fun animateBottomNavUp() {
        binding.bottomNav.translationY = 500f
        binding.bottomNav.animate().translationY(0f).start()
    }

    private fun animateBottomNavDown() {
        binding.bottomNav.animate().translationY(500f).start()
    }

    @Parcelize
    data class Args(val moveToLibrary: Boolean =  false) : BaseArgs(), Parcelable
}