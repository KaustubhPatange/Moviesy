package com.kpstv.yts.ui.settings

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.common_moviesy.extensions.globalVisibleRect
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppPreference
import com.kpstv.yts.R
import com.kpstv.yts.AppSettings.IS_DARK_THEME_PREF
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.ui.fragments.SettingFragment
import com.kpstv.yts.ui.helpers.ThemeHelper

class LookSettingsFragment : PreferenceFragmentCompat() {
    private val appPreference by defaultPreference()
    interface ThemeChangeCallbacks {
        fun onThemeChanged(viewRect: Rect)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_preference, rootKey)

        findPreference<SwitchPreferenceCompat>(IS_DARK_THEME_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            val dark = newValue as Boolean
            val theme = if (dark) ThemeHelper.AppTheme.DARK else ThemeHelper.AppTheme.LIGHT
            appPreference.setTheme(theme)

            val switchView = requireView().findViewById<View>(R.id.switchWidget)
            (parentFragment as ThemeChangeCallbacks).onThemeChanged(switchView.globalVisibleRect())
            true
        }
    }
}