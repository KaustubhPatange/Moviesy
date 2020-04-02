package com.kpstv.yts.ui.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R

class GeneralSettingsFragment: PreferenceFragmentCompat() {
    private val TAG = "GeneralSettingsFragment"
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_preference, rootKey)

        val anonyPref = findPreference<SwitchPreferenceCompat>("anonymous_torrent")
        anonyPref?.isChecked = AppInterface.ANONYMOUS_TORRENT_DOWNLOAD
        anonyPref?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.ANONYMOUS_TORRENT_DOWNLOAD = newValue as Boolean
            return@setOnPreferenceChangeListener true
        }

        val tmdbPref = findPreference<EditTextPreference>("tmdb_api_url")
        tmdbPref?.text = AppInterface.TMDB_BASE_URL
        tmdbPref?.summary = AppInterface.TMDB_BASE_URL
        tmdbPref?.setOnPreferenceChangeListener { _, newValue ->
            tmdbPref.summary = AppInterface.TMDB_BASE_URL
            AppInterface.TMDB_BASE_URL = newValue.toString().trim()
            return@setOnPreferenceChangeListener true
        }

        val yifyPref = findPreference<EditTextPreference>("yify_web_url")
        yifyPref?.text = AppInterface.YIFY_BASE_URL
        yifyPref?.summary = AppInterface.YIFY_BASE_URL
        yifyPref?.setOnPreferenceChangeListener { _, newValue ->
            yifyPref.summary = AppInterface.YIFY_BASE_URL
            AppInterface.YIFY_BASE_URL = newValue.toString().trim()
            return@setOnPreferenceChangeListener true
        }

        val ytsPref = findPreference<EditTextPreference>("yts_web_url")
        ytsPref?.text = AppInterface.YTS_BASE_URL
        ytsPref?.summary = AppInterface.YTS_BASE_URL
        ytsPref?.setOnPreferenceChangeListener { _, newValue ->
            ytsPref.summary = AppInterface.YTS_BASE_URL
            AppInterface.YTS_BASE_URL = newValue.toString().trim()
            return@setOnPreferenceChangeListener true
        }
    }
}