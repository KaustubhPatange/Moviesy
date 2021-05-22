package com.kpstv.yts.ui.helpers

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kpstv.common_moviesy.extensions.getColorAttr
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppPreference
import com.kpstv.yts.R

object ThemeHelper {
    private var currentTheme = AppTheme.DARK

    fun getTheme() = currentTheme

    fun isLightVariantTheme() : Boolean {
        return currentTheme == AppTheme.LIGHT
    }

    fun isDarkVariantTheme() : Boolean {
        return currentTheme == AppTheme.DARK
    }

    fun updateValues(appPreference: AppPreference) {
        currentTheme = appPreference.getTheme()
    }

    enum class AppTheme(@StyleRes val style: Int) {
        DARK(R.style.StartTheme_Dark),
        LIGHT(R.style.StartTheme_Light)
    }

    fun Context.updateTheme(activity: FragmentActivity) {
        val decorView = activity.window.decorView

        val style = when (currentTheme) {
            AppTheme.DARK -> AppTheme.DARK.style
            AppTheme.LIGHT -> AppTheme.LIGHT.style
        }

        theme.applyStyle(style, true)

        if (Build.VERSION.SDK_INT < 23) {
            activity.window.statusBarColor = Color.BLACK
            activity.window.navigationBarColor = Color.BLACK
        } else if (Build.VERSION.SDK_INT >= 23) {
            val color = activity.getColorAttr(R.attr.colorForeground)
            activity.window.statusBarColor = color
            activity.window.navigationBarColor = color

            when(currentTheme) {
                AppTheme.DARK -> decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                AppTheme.LIGHT -> decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        activity.window.setBackgroundDrawable(
            ColorDrawable(activity.getColorAttr(R.attr.colorBackground))
        )
    }

    /**
     * Once the theme is changed the fragment will be respond to the theme changes.
     */
    fun Fragment.registerForThemeChange() {
        this.lifecycle.addObserver(object: DefaultLifecycleObserver{
            override fun onCreate(owner: LifecycleOwner) {
                context?.updateTheme(requireActivity())
                lifecycle.removeObserver(this)
                super.onCreate(owner)
            }
        })
    }
}