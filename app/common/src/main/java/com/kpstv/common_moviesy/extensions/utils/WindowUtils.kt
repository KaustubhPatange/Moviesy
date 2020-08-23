package com.kpstv.common_moviesy.extensions.utils

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

object WindowUtils {
    /**
     * Must be called in the onResume method
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun activateFullScreen(activity: AppCompatActivity) = with(activity) {
        val decorView = window.decorView
        val uiOptions =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }
}