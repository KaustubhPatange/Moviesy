package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.common_moviesy.extensions.registerFragmentLifecycleForLogging
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.AppInterface.Companion.setAppThemeMain
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.databinding.ActivityMainBinding
import com.kpstv.yts.extensions.NavigationModel
import com.kpstv.yts.extensions.NavigationModels
import com.kpstv.yts.extensions.Navigations
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.UpdateUtils
import com.kpstv.yts.services.CastTorrentService
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.ui.fragments.HomeFragment
import com.kpstv.yts.ui.fragments.LibraryFragment
import com.kpstv.yts.ui.fragments.WatchlistFragment
import com.kpstv.yts.ui.helpers.ChangelogHelper
import com.kpstv.yts.ui.helpers.MainCastHelper
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.settings.SettingsActivity
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import io.github.dkbai.tinyhttpd.nanohttpd.webserver.SimpleWebServer
import javax.inject.Inject
import kotlin.reflect.KClass

@AndroidEntryPoint
class MainActivity : AbstractBottomNavActivity() {

    @Inject
    lateinit var updateUtils: UpdateUtils

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(ActivityMainBinding::inflate)

    val TAG = "MainActivity"

    val castHelper = CastHelper()
    private val mainCastHelper by lazy {
        MainCastHelper(this, castHelper)
    }

    lateinit var drawerLayout: DrawerLayout

    private var isDarkTheme = true
    private val navigations by lazy {
        Navigations(this)
    }

    override val bottomNavigationViewId: Int get() = R.id.bottom_nav
    override val fragmentContainerId: Int get() = R.id.fragment_container
    override val bottomNavFragments: MutableMap<Int, KClass<out Fragment>> = mutableMapOf(
        R.id.homeFragment to HomeFragment::class,
        R.id.watchFragment to WatchlistFragment::class,
        R.id.libraryFragment to LibraryFragment::class
    )

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppThemeMain(this)
        setContentView(binding.root)

        SimpleWebServer.init(this, BuildConfig.DEBUG)

        isDarkTheme = IS_DARK_THEME

        if (CastHelper.isCastingSupported(this)) {
            castHelper.initCastSession(this)
            mainCastHelper.setUpCastRelatedStuff()
        }

        setNavigationDrawer()

        drawerLayout = binding.drawerLayout

        setPremiumButtonClicked()

        setNavigationDrawerItemClicks()

        ChangelogHelper(this).show()

        // TODO: Remove this logging
        /*registerFragmentLifecycleForLogging { fragment, which ->
            Log.e(fragment::class.java.simpleName, "-> $which")
        }*/
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /** Check for intent arguments */
        checkIntentArguments()

        /** Check for updates */
        updateUtils.check(
            onUpdateAvailable = {
                updateUtils.showUpdateDialog(this) {
                    updateUtils.processUpdate(it)
                    Toasty.info(this, getString(R.string.update_download_text)).show()
                }
            },
            onUpdateNotFound = {
                checkForAutoPurchase()
            },
            onVersionDeprecated = {
                AppUtils.doOnVersionDeprecated(this)
            },
            onError = {
                Toasty.error(this, "Failed: ${it.message}").show()
            }
        )
    }

    private fun setNavigationDrawer() = Coroutines.main {
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
        viewModel.pauseMovieJob.observe(this, Observer {
            navigations.updateNotification(NAV_DOWNLOAD_QUEUE, it.size)
        })
    }

    private fun setNavigationDrawerItemClicks() {
        binding.navigationLayout.ivReport.setOnClickListener {
            AppUtils.launchUrl(this, "${getString(R.string.app_github)}/issues", IS_DARK_THEME)
        }
        binding.navigationLayout.ivShare.setOnClickListener {
            AppUtils.shareApp(this)
        }
    }

    private fun setPremiumButtonClicked() {
        binding.navigationLayout.customDrawerPremium.root.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            if (!AppInterface.IS_PREMIUM_UNLOCKED) {
                PremiumHelper.openPurchaseFragment(this)
            } else {
                PremiumHelper.showPremiumAlreadyPurchasedDialog(this)
            }
        }
    }

    private fun checkIntentArguments() {
        if (intent?.getBooleanExtra(SplashActivity.ARG_ROUTE_TO_LIBRARY, false) == true) {
            binding.bottomNav.selectedItemId = R.id.libraryFragment
            setBottomNavFragment(R.id.libraryFragment)
        }
    }

    private fun navigateTo(tag: String) {
        drawerLayout.closeDrawer(GravityCompat.START)
        when (tag) {
            NAV_DOWNLOAD_QUEUE -> {
                val downloadIntent = Intent(this, DownloadActivity::class.java)
                startActivity(downloadIntent)
            }
            NAV_SETTINGS -> {
                val settingIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingIntent)
            }
        }
    }

    private fun checkForAutoPurchase() {
        PremiumHelper.scanForAutoPurchase(this,
            onPremiumActivated = {
                PremiumHelper.showPremiumActivatedDialog(this)
            },
            onNoPremiumFound = {
                PremiumHelper.showPurchaseInfo(this)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (IS_DARK_THEME != isDarkTheme) {
            val previousIntent = intent
            finish()
            startActivity(previousIntent)
        }
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            getSelectedBottomNavFragmentId() != R.id.homeFragment -> binding.bottomNav.selectedItemId = R.id.homeFragment
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        stopService(Intent(this, DownloadService::class.java))
        stopService(Intent(this, CastTorrentService::class.java))
        /** Stop the HTTP server if started */
        SimpleWebServer.stopServer()
        super.onDestroy()
    }


    companion object {
        const val NAV_DOWNLOAD_QUEUE = "nav_download_queue"
        const val NAV_SETTINGS = "nav_settings"
    }
}