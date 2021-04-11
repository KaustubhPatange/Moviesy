package com.kpstv.yts.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.kpstv.common_moviesy.extensions.makeFullScreen
import com.kpstv.common_moviesy.extensions.registerFragmentLifecycleForLogging
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter
import com.kpstv.navigation.canFinish
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.databinding.ActivityStartBinding
import com.kpstv.yts.extensions.errors.SSLHandshakeException
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.fragments.*
import com.kpstv.yts.ui.helpers.ActivityIntentHelper
import com.kpstv.yts.ui.helpers.InitializationHelper
import com.kpstv.yts.ui.helpers.MainCastHelper2
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.dkbai.tinyhttpd.nanohttpd.webserver.SimpleWebServer
import javax.inject.Inject
import kotlin.reflect.KClass

@AndroidEntryPoint
class StartActivity : AppCompatActivity(), NavigatorTransmitter, LibraryFragment2.Callbacks {
    // TODO: Implement deeplinks
    private val binding by viewBinding(ActivityStartBinding::inflate)
    private val navViewModel by viewModels<StartViewModel>()
    private val intentHelper by lazy { ActivityIntentHelper(navViewModel) }
    private val castHelper = CastHelper()
    private val mainCastHelper by lazy { MainCastHelper2(this, lifecycle, castHelper) }

    private lateinit var navigator: Navigator

    @Inject
    lateinit var initializationHelper: InitializationHelper

    override fun getNavigator(): Navigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        navigator = Navigator(supportFragmentManager, binding.fragmentContainer)

        navViewModel.navigation.observe(this, navigationObserver)
        navViewModel.errors.observe(this, errorObserver)

        setAutoZIndex()

        if (savedInstanceState == null && !intentHelper.handle(intent)) {
            navViewModel.navigateTo(Screen.SPLASH)
        }

        if (CastHelper.isCastingSupported(this)) { // TODO: Move to Activity Level
            SimpleWebServer.init(this, BuildConfig.DEBUG)
            castHelper.initCastSession(this)
            mainCastHelper.setUpCastRelatedStuff()
        }

        /*registerFragmentLifecycleForLogging { fragment, state ->
            Log.e(fragment::class.simpleName, "=> $state")
        }*/
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intentHelper.handle(intent)
    }

    override fun onStart() {
        super.onStart()
        initializationHelper.initializeDependencies()
    }

    override fun getCastHelper(): CastHelper = castHelper

    private val navigationObserver = Observer { navOptions: Navigator.NavOptions? ->
        navOptions?.let { navigator.navigateTo(it) }
    }

    private val errorObserver = Observer { error: Exception? ->
        when(error) {
            is SplashErrorException -> {
                if (error.inner is SSLHandshakeException) {
                    AppUtils.showSSLHandshakeDialog(this) { finish() }
                } else {
                    AppUtils.showUnknownErrorDialog(this, error.inner) { finish() }
                }
            }
        }
    }

    // To solve some overlapping issues with Fragment add transaction.
    private fun setAutoZIndex() {
        supportFragmentManager.addOnBackStackChangedListener {
            binding.fragmentContainer.children.forEachIndexed { index, view ->
                view.translationZ = (index + 1).toFloat()
            }
        }
    }

    enum class Screen(val clazz: KClass<out Fragment>) {
        SPLASH (SplashFragment::class),
        WELCOME_DISCLAIMER (WelcomeDisclaimerFragment::class),
        WELCOME_CARRIER_DISCLAIMER (WelcomeCarrierFragment::class),
        MAIN (MainFragment::class),
        SETTING (SettingFragment::class),
        MORE (MoreFragment::class),
        SEARCH (SearchFragment::class),
        DETAIL (DetailFragment::class)
    }

    override fun onBackPressed() {
        if (navigator.canFinish()) super.onBackPressed()
    }

    override fun onDestroy() {
        SimpleWebServer.stopServer()
        super.onDestroy()
    }
}