package com.kpstv.yts.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.IS_FIRST_LAUNCH_PREF
import com.kpstv.yts.AppInterface.Companion.PROXY_CHECK_PREF
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.AppUtils.Companion.parseSettings
import com.kpstv.yts.extensions.utils.ProxyUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import javax.inject.Inject

/** Starting point of reading this app.
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), Animation.AnimationListener {

    @Inject lateinit var proxyUtils: ProxyUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /** If this app is launched first time or suppose from previous use
         *  this was an error in making okHttp request (could be anything).
         *
         *  In such case we will check the database when this app is launched
         *  to see if there is an updated proxy for YTS or any other api. */
       // proxyCheckPreference()

        val rotate = AnimationUtils.loadAnimation(this, R.anim.anim_splah_play)
        rotate.setAnimationListener(this)
        imageView.startAnimation(rotate)

        val fade = AnimationUtils.loadAnimation(this, R.anim.anim_splash_text)
        textView.startAnimation(fade)

        /** This will set settings from default app preference
         */
        parseSettings(this)

    }

    private suspend fun proxyCheckPreference(): Boolean {
        val settingPref = PreferenceManager.getDefaultSharedPreferences(this)
        if (settingPref.getBoolean(PROXY_CHECK_PREF, false) ||
                settingPref.getBoolean(IS_FIRST_LAUNCH_PREF, true)) {
            proxyUtils.check()
            return true
        }
        return false
    }

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
