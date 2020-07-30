package com.kpstv.yts.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.kpstv.yts.AppInterface.Companion.IS_FIRST_LAUNCH_PREF
import com.kpstv.yts.AppInterface.Companion.PROXY_CHECK_PREF
import com.kpstv.yts.AppSettings.parseSettings
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ActivitySplashBinding
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.extensions.utils.ProxyUtils
import com.kpstv.yts.extensions.viewBinding
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Starting point of reading this app.
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), Animation.AnimationListener {

    @Inject
    lateinit var proxyUtils: ProxyUtils

    private val binding by viewBinding(ActivitySplashBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val rotate = AnimationUtils.loadAnimation(this, R.anim.anim_splah_play)
        binding.imageView.startAnimation(rotate)

        /** If this app is launched first time or suppose from previous use
         *  this was an error in making okHttp request (could be anything).
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
        val settingPref = PreferenceManager.getDefaultSharedPreferences(this)
        if (settingPref.getBoolean(PROXY_CHECK_PREF, false) ||
            settingPref.getBoolean(IS_FIRST_LAUNCH_PREF, true)
        ) {
            binding.progressBar.show()
            /** A progressBar effect */
            proxyUtils.check(
                onComplete = {
                    binding.progressBar.hide()
                    block.invoke()
                },
                onError = { e ->
                    AlertNoIconDialog.Companion.Builder(this)
                        .setTitle(getString(R.string.error))
                        .setMessage(e.message ?: getString(R.string.error_unknown))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.dismiss)) { finish() }
                        .show()
                }
            )
        } else {
            Log.e(TAG, "No need to check for proxy")
            block.invoke()
        }
    }

    private val TAG = javaClass.simpleName
    override fun onAnimationRepeat(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(300)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onAnimationStart(animation: Animation?) {

    }
}
