package com.kpstv.yts.ui.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings.ANONYMOUS_TORRENT_DOWNLOAD_PREF
import com.kpstv.yts.AppSettings.TMDB_BASE_URL_PREF
import com.kpstv.yts.AppSettings.YIFY_BASE_URL_PREF
import com.kpstv.yts.AppSettings.YTS_BASE_URL_PREF
import com.kpstv.yts.R
import com.kpstv.yts.extensions.SearchType

class GeneralSettingsFragment : PreferenceFragmentCompat() {
    private val TAG = "GeneralSettingsFragment"

    companion object {
        const val SUGGESTION_TYPE_PREF = "suggestion_type_pref"
        const val LATEST_MOVIE_NOTIFY_PREF = "latest_movie_notify_pref"
        const val FEATURED_MOVIE_NOTIFY_PREF = "featured_movie_notify_pref"
    }

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

        val searchTypes = SearchType.values().map { it.name }.toTypedArray()

        val suggestionType = findPreference<ListPreference>(SUGGESTION_TYPE_PREF)
        suggestionType?.entries = searchTypes
        suggestionType?.entryValues = searchTypes
        suggestionType?.setDefaultValue(AppInterface.SUGGESTION_SEARCH_TYPE.name)
        suggestionType?.setOnPreferenceChangeListener { _, value ->
            AppInterface.SUGGESTION_SEARCH_TYPE = SearchType.valueOf(value.toString())
            true
        }
    }
}