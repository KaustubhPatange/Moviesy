package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kpstv.after.After
import com.kpstv.after.AfterRequests
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.AppSettings
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentSplashBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.extensions.errors.SSLHandshakeException
import com.kpstv.yts.extensions.utils.ProxyUtils
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.navigation.KeyedFragment
import com.kpstv.navigation.Navigator
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * [SSLHandshakeException] or [Exception]
 */
class SplashErrorException(val inner: Exception) : Exception()

@AndroidEntryPoint
class SplashFragment : KeyedFragment(R.layout.fragment_splash)  {
    @Inject lateinit var proxyUtils: ProxyUtils

    private val binding by viewBinding(FragmentSplashBinding::bind)
    private val appNavViewModel by activityViewModels<StartViewModel>()
    private val appPreference by defaultPreference()

    private lateinit var afterRequests: AfterRequests

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animateLogo()
        /** If this app is launched first time or suppose from previous use
         *  there was an error in making okHttp request or any other crash.
         *
         *  In such case we will check the database when this app is launched
         *  to see if there is an updated proxy for YTS or any other api. */

        proxyCheckPreference {
            AppSettings.parseSettings(requireContext())
            animateText(::onProxyCheckComplete)
        }
    }

    private fun onProxyCheckComplete() {
        if (!appPreference.isFirstLaunch())
            callMainActivity()
        else {
            appNavViewModel.navigateTo(
                screen = StartActivity.Screen.WELCOME_DISCLAIMER,
                args = WelcomeDisclaimerFragment.args,
                transition = Navigator.TransitionType.CIRCULAR
            )
        }
    }

    private fun callMainActivity() {
        appNavViewModel.navigateTo(
            screen = StartActivity.Screen.MAIN,
            transition = Navigator.TransitionType.FADE,
//            args = MainFragment.Args(moveToDetail = DetailFragment.Args(ytsId = 28832)),
            popUpTo = true
        )
    }

    private fun proxyCheckPreference(block: () -> Unit) {
        if (appPreference.shouldCheckProxy() || appPreference.isFirstLaunch()) {
            binding.progressBar.show()

            dispatchAfterEvents()

            proxyUtils.check(
                context = viewLifecycleOwner.lifecycleScope.coroutineContext,
                onComplete = {
                    binding.progressBar.hide()
                    block.invoke()
                },
                onError = { e ->
                    if (::afterRequests.isInitialized) afterRequests.stop()
                    appNavViewModel.propagateError(SplashErrorException(e))
                }
            )
        } else {
            block.invoke()
        }
    }

    private fun dispatchAfterEvents() {
        val defaultOptions =
            After.Options(displayLocation = After.Location.TOP, emoji = After.Emoji.HAPPY)
        val nextOptions =
            After.Options(displayLocation = After.Location.TOP, emoji = After.Emoji.SAD)

        afterRequests = After.time(10, TimeUnit.SECONDS)
            .prompt(requireContext(), getString(R.string.proxy_no_worries), defaultOptions) {
                After.time(5, TimeUnit.SECONDS)
                    .prompt(requireContext(), getString(R.string.this_much_time), nextOptions)
            }
    }

    private fun animateText(onEnd: SimpleCallback) {
        binding.textView.alpha = 0f
        binding.textView.show()
        binding.textView.animate().alpha(1f)
            .withEndAction(onEnd)
            .start()
    }

    private fun animateLogo() {
        binding.ivLogo.scaleX = 0f
        binding.ivLogo.scaleY = 0f
        binding.ivLogo.alpha = 0f

        binding.ivLogo.animate()
            .scaleX(1f).scaleY(1f)
            .rotation(360f)
            .alpha(1f)
            .start()
    }

    override fun onDestroyView() {
        if (::afterRequests.isInitialized) afterRequests.stop()
        super.onDestroyView()
    }
}