package com.kpstv.yts.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.AppSettings.IS_DARK_THEME_PREF

class LookSettingsFragment(
    private val onThemeChange : (Boolean) -> Unit
): PreferenceFragmentCompat() {

    lateinit var onDarkThemeChangeListener:(Boolean) -> Unit

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_preference, rootKey)

        findPreference<SwitchPreferenceCompat>(IS_DARK_THEME_PREF)?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.IS_DARK_THEME = newValue as Boolean
            onThemeChange.invoke(newValue)
            true
        }
      /*  val darkPref = findPreference<SwitchPreferenceCompat>(IS_DARK_THEME_PREF)
        darkPref?.isChecked = AppInterface.IS_DARK_THEME
        darkPref?.setOnPreferenceChangeListener { _, newValue ->
            onDarkThemeChangeListener.invoke(newValue as Boolean)
            return@setOnPreferenceChangeListener true
        }*/
    }
}