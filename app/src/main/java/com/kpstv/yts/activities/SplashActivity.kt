package com.kpstv.yts.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.kpstv.yts.R
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity(), Animation.AnimationListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val rotate = AnimationUtils.loadAnimation(this, R.anim.anim_splah_play)
        rotate.setAnimationListener(this)
        imageView.startAnimation(rotate)

        val fade = AnimationUtils.loadAnimation(this, R.anim.anim_splash_text)
        textView.startAnimation(fade)
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
