package com.kpstv.yts.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R

class LookSettingsFragment: PreferenceFragmentCompat() {

    lateinit var onDarkThemeChangeListener:(Boolean) -> Unit

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.look_preference, rootKey)

        val darkPref = findPreference<SwitchPreferenceCompat>("IS_DARK_THEME")
        darkPref?.isChecked = AppInterface.IS_DARK_THEME
        darkPref?.setOnPreferenceChangeListener { _, newValue ->
            onDarkThemeChangeListener.invoke(newValue as Boolean)
            return@setOnPreferenceChangeListener true
        }
    }
}