package com.kpstv.common_moviesy.extensions

import android.app.Activity
import android.view.View

fun Activity.makeFullScreen() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
}