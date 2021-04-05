package com.kpstv.yts.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.kpstv.common_moviesy.extensions.makeFullScreen
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.databinding.ActivityStartBinding
import com.kpstv.yts.extensions.errors.SSLHandshakeException
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.fragments.*
import com.kpstv.yts.ui.helpers.InitializationHelper
import com.kpstv.yts.ui.navigation.Navigator
import com.kpstv.yts.ui.navigation.NavigatorTransmitter
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.reflect.KClass

@AndroidEntryPoint
class StartActivity : AppCompatActivity(), NavigatorTransmitter {
    // TODO: Implement deeplinks
    private val binding by viewBinding(ActivityStartBinding::inflate)
    private val navViewModel by viewModels<StartViewModel>()

    private lateinit var navigator: Navigator

    @Inject
    lateinit var initializationHelper: InitializationHelper

    override fun getNavigator(): Navigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        navigator = Navigator(window, supportFragmentManager, binding.fragmentContainer.id)

        navViewModel.navigation.observe(this, navigationObserver)
        navViewModel.errors.observe(this, errorObserver)

        navViewModel.navigateTo(Screen.SPLASH)
    }

    override fun onStart() {
        super.onStart()
        initializationHelper.initializeDependencies()
    }

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

    enum class Screen(val clazz: KClass<out Fragment>) {
        SPLASH (SplashFragment::class),
        WELCOME_DISCLAIMER (WelcomeDisclaimerFragment::class),
        WELCOME_CARRIER_DISCLAIMER (WelcomeCarrierFragment::class),
        MAIN (MainFragment::class),
        SETTING (SettingFragment::class)
    }

    override fun onBackPressed() {
        if (navigator.canGoBack() && navigator.goBack())
            return
        super.onBackPressed()
    }
}