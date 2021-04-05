package com.kpstv.yts.ui.fragments

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ActivityMainBinding
import com.kpstv.yts.extensions.NavigationModel
import com.kpstv.yts.extensions.NavigationModels
import com.kpstv.yts.extensions.Navigations
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.activities.DownloadActivity
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.helpers.ChangelogHelper
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.helpers.ThemeHelper.updateTheme
import com.kpstv.yts.ui.navigation.KeyedFragment
import com.kpstv.yts.ui.settings.SettingsActivity
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : KeyedFragment(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val viewModel by viewModels<MainViewModel>()
    private val navigations by lazy {
        Navigations(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.updateTheme()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.setBackgroundDrawable(
            ColorDrawable(requireContext().getColorAttr(R.attr.colorBackground))
        )
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            root.applyTopInsets(navigationLayout.navRootLayout)
            root.applyBottomInsets(navigationLayout.navRootLayout)
        }

        animateBottomNavUp()
        setNavigationDrawer()
        setNavigationDrawerItemClicks()
        setPremiumButtonClicked()

        ChangelogHelper(requireActivity()).show() // TODO: Update this
    }

    private fun setNavigationDrawer() = Coroutines.main {
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
        viewModel.pauseMovieJob.observe(viewLifecycleOwner, Observer {
            navigations.updateNotification(MainActivity.NAV_DOWNLOAD_QUEUE, it.size) // TODO: Update this
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
                val settingIntent = Intent(requireContext(), SettingsActivity::class.java)
                startActivity(settingIntent)
            }
        }
    }

    private fun setPremiumButtonClicked() {
        binding.navigationLayout.customDrawerPremium.root.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            if (!AppInterface.IS_PREMIUM_UNLOCKED) {
                PremiumHelper.openPurchaseFragment(requireActivity()) // TODO: Update this
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
}