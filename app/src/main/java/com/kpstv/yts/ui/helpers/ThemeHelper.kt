package com.kpstv.yts.ui.helpers

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.kpstv.common_moviesy.extensions.getColorAttr
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R

object ThemeHelper {
    enum class AppTheme(@StyleRes val style: Int) {
        DARK(R.style.StartTheme_Dark),
        LIGHT(R.style.StartTheme_Light)
    }

    fun Context.updateTheme(activity: FragmentActivity) {
        val decorView = activity.window.decorView

        val style = if (AppInterface.IS_DARK_THEME) {
            AppTheme.DARK.style
        } else
            AppTheme.LIGHT.style

        theme.applyStyle(style, true)

        if (Build.VERSION.SDK_INT < 23) {
            activity.window.statusBarColor = Color.BLACK
            activity.window.navigationBarColor = Color.BLACK
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (!AppInterface.IS_DARK_THEME) {
                decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }

        activity.window.setBackgroundDrawable(
            ColorDrawable(activity.getColorAttr(R.attr.colorBackground))
        )
    }

    /**
     * Once the theme is changed the fragment will be respond to the theme changes.
     */
    fun Fragment.registerForThemeChange(fm: FragmentManager) {
        fm.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentCreated(
                fm: FragmentManager,
                f: Fragment,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentCreated(fm, f, savedInstanceState)
                if (this@registerForThemeChange::class == f::class) {
                    context?.updateTheme(requireActivity())
                    fm.unregisterFragmentLifecycleCallbacks(this)
                }
            }
        }, false)
    }
}