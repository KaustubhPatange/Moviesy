package com.kpstv.yts.ui.helpers

import android.content.Context
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R

object ThemeHelper {
    fun Context.updateTheme() {
        val style = if (AppInterface.IS_DARK_THEME) {
            R.style.StartTheme_Dark
        } else
            R.style.StartTheme_Light
        theme.applyStyle(style, true)

    }
}