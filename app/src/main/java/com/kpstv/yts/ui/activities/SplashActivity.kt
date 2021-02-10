package com.kpstv.yts.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.after.After
import com.kpstv.after.AfterRequests
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.AppInterface.Companion.IS_FIRST_LAUNCH_PREF
import com.kpstv.yts.AppInterface.Companion.PROXY_CHECK_PREF
import com.kpstv.yts.AppSettings.parseSettings
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ActivitySplashBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.startActivityAndFinish
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.ProxyUtils
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Starting point of reading this app.
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), Animation.AnimationListener {

    companion object {
        const val ARG_ROUTE_TO_LIBRARY = "arg_route_to_library" // Type Boolean
    }

    @Inject
    lateinit var proxyUtils: ProxyUtils

    private val binding by viewBinding(ActivitySplashBinding::inflate)
    private val appPreference by defaultPreference()
    private lateinit var afterRequests: AfterRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val rotate = AnimationUtils.loadAnimation(this, R.anim.anim_splah_play)
        binding.imageView.startAnimation(rotate)

        /** If this app is launched first time or suppose from previous use
         *  there was an error in making okHttp request or any other crash.
         *
         *  In such case we will check the database when this app is launched
         *  to see if there is an updated proxy for YTS or any other api. */
        proxyCheckPreference {
            val fade = AnimationUtils.loadAnimation(this, R.anim.anim_splash_text)
            fade.setAnimationListener(this)
            binding.textView.show()
            binding.textView.startAnimation(fade)

            /** This will set settings from default app preference
             */
            parseSettings(this)
        }
    }

    private fun proxyCheckPreference(block: () -> Unit) {
        if (appPreference.getBoolean(PROXY_CHECK_PREF, false) ||
            appPreference.getBoolean(IS_FIRST_LAUNCH_PREF, true)
        ) {
            binding.progressBar.show()
            /** A progressBar effect */
            proxyUtils.check(
                onComplete = {
                    binding.progressBar.hide()
                    block.invoke()
                },
                onError = { e ->
                    if (::afterRequests.isInitialized) afterRequests.stop()

                    AlertNoIconDialog.Companion.Builder(this)
                        .setTitle(getString(R.string.error))
                        .setMessage(e.message ?: getString(R.string.error_unknown))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.dismiss)) { finish() }
                        .setNegativeButton(getString(R.string.need_help)) {
                            AppUtils.launchUrlIntent(this, getString(R.string.app_help))
                            finish()
                        }
                        .show()
                }
            )

            dispatchAfterEvents()
        } else {
            Log.e(TAG, "No need to check for proxy")
            block.invoke()
        }
    }

    private fun dispatchAfterEvents() {
        val defaultOptions =
            After.Options(displayLocation = After.Location.TOP, emoji = After.Emoji.HAPPY)
        val nextOptions =
            After.Options(displayLocation = After.Location.TOP, emoji = After.Emoji.SAD)

        afterRequests = After.time(10, TimeUnit.SECONDS)
            .prompt(this, getString(R.string.proxy_no_worries), defaultOptions) {
                After.time(5, TimeUnit.SECONDS)
                    .prompt(this, getString(R.string.this_much_time), nextOptions)
            }
    }

    private val TAG = javaClass.simpleName
    override fun onAnimationRepeat(animation: Animation?) {}

    override fun onAnimationEnd(animation: Animation?) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(300)
            if (appPreference.getBoolean(AgreementActivity.SHOW_AGREEMENT_PREF, false))
                callMainActivity()
            else startActivityAndFinish(Intent(this@SplashActivity, AgreementActivity::class.java))
        }
    }

    override fun onAnimationStart(animation: Animation?) {}

    private fun callMainActivity() {
        val routeToLibrary = intent?.getBooleanExtra(ARG_ROUTE_TO_LIBRARY, false)
        val navigateIntent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            putExtra(ARG_ROUTE_TO_LIBRARY, routeToLibrary)
        }
        startActivityAndFinish(navigateIntent)
    }

    override fun onDestroy() {
        if (::afterRequests.isInitialized) afterRequests.stop()
        super.onDestroy()
    }
}
