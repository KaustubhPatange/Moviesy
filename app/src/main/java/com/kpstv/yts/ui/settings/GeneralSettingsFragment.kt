package com.kpstv.yts.ui.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.AppSettings.ANONYMOUS_TORRENT_DOWNLOAD_PREF
import com.kpstv.yts.AppSettings.TMDB_BASE_URL_PREF
import com.kpstv.yts.AppSettings.YIFY_BASE_URL_PREF
import com.kpstv.yts.AppSettings.YTS_BASE_URL_PREF

class GeneralSettingsFragment: PreferenceFragmentCompat() {
    private val TAG = "GeneralSettingsFragment"
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_preference, rootKey)

        val anonyPref = findPreference<SwitchPreferenceCompat>(ANONYMOUS_TORRENT_DOWNLOAD_PREF)
        anonyPref?.isChecked = AppInterface.ANONYMOUS_TORRENT_DOWNLOAD
        anonyPref?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.ANONYMOUS_TORRENT_DOWNLOAD = newValue as Boolean
            return@setOnPreferenceChangeListener true
        }

        val tmdbPref = findPreference<EditTextPreference>(TMDB_BASE_URL_PREF)
        tmdbPref?.text = AppInterface.TMDB_BASE_URL
        tmdbPref?.summary = AppInterface.TMDB_BASE_URL
        tmdbPref?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.TMDB_BASE_URL = newValue.toString().trim()
            tmdbPref.summary = AppInterface.TMDB_BASE_URL
            return@setOnPreferenceChangeListener true
        }

        val yifyPref = findPreference<EditTextPreference>(YIFY_BASE_URL_PREF)
        yifyPref?.text = AppInterface.YIFY_BASE_URL
        yifyPref?.summary = AppInterface.YIFY_BASE_URL
        yifyPref?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.YIFY_BASE_URL = newValue.toString().trim()
            yifyPref.summary = AppInterface.YIFY_BASE_URL
            return@setOnPreferenceChangeListener true
        }

        val ytsPref = findPreference<EditTextPreference>(YTS_BASE_URL_PREF)
        ytsPref?.text = AppInterface.YTS_BASE_URL
        ytsPref?.summary = AppInterface.YTS_BASE_URL
        ytsPref?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.YTS_BASE_URL = newValue.toString().trim()
            ytsPref.summary = AppInterface.YTS_BASE_URL
            return@setOnPreferenceChangeListener true
        }
    }
}