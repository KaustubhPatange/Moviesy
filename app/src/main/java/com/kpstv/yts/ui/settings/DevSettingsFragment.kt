package com.kpstv.yts.ui.settings

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.AppSettings
import com.kpstv.yts.extensions.utils.AppSettings.COUNTRY_FLAG_JSON_URL_PREF
import com.kpstv.yts.extensions.utils.AppSettings.CUSTOM_LAYOUT_YTS_SPAN_PREF
import com.kpstv.yts.extensions.utils.AppSettings.DOWNLOAD_CONNECTION_TIMEOUT_PREF
import com.kpstv.yts.extensions.utils.AppSettings.DOWNLOAD_TIMEOUT_SECOND_PREF
import com.kpstv.yts.extensions.utils.AppSettings.MOVIE_SPAN_DIFFERENCE_PREF
import com.kpstv.yts.extensions.utils.AppSettings.QUERY_SPAN_DIFFERENCE_PREF
import com.kpstv.yts.extensions.utils.AppSettings.SUGGESTION_URL_PREF
import com.kpstv.yts.extensions.utils.AppSettings.TMDB_API_KEY_PREF
import com.kpstv.yts.extensions.utils.AppSettings.TMDB_IMAGE_PREFIX_PREF

class DevSettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.developer_preference, rootKey)

        val su = findPreference<EditTextPreference>(SUGGESTION_URL_PREF)
        su?.text = AppInterface.SUGGESTION_URL
        su?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.SUGGESTION_URL = newValue.toString()
            return@setOnPreferenceChangeListener true
        }

        val cfju = findPreference<EditTextPreference>(COUNTRY_FLAG_JSON_URL_PREF)
        cfju?.text = AppInterface.COUNTRY_FLAG_JSON_URL
        cfju?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.COUNTRY_FLAG_JSON_URL = newValue.toString()
            return@setOnPreferenceChangeListener true
        }

        val tip = findPreference<EditTextPreference>(TMDB_IMAGE_PREFIX_PREF)
        tip?.text = AppInterface.TMDB_IMAGE_PREFIX
        tip?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.TMDB_IMAGE_PREFIX = newValue.toString()
            return@setOnPreferenceChangeListener true
        }

        val tak = findPreference<EditTextPreference>(TMDB_API_KEY_PREF)
        tak?.text = AppInterface.TMDB_API_KEY
        tak?.summary = AppInterface.TMDB_API_KEY
        tak?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.TMDB_API_KEY = newValue.toString()
            return@setOnPreferenceChangeListener true
        }

        val clys = findPreference<EditTextPreference>(CUSTOM_LAYOUT_YTS_SPAN_PREF)
        clys?.text = "${AppInterface.CUSTOM_LAYOUT_YTS_SPAN}"
        clys?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
        clys?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.CUSTOM_LAYOUT_YTS_SPAN = newValue.toString().toInt()
            return@setOnPreferenceChangeListener true
        }

        val qsd = findPreference<EditTextPreference>(QUERY_SPAN_DIFFERENCE_PREF)
        qsd?.text = "${AppInterface.QUERY_SPAN_DIFFERENCE}"
        qsd?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
        qsd?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.QUERY_SPAN_DIFFERENCE = newValue.toString().toInt()
            return@setOnPreferenceChangeListener true
        }

        val msd = findPreference<EditTextPreference>(MOVIE_SPAN_DIFFERENCE_PREF)
        msd?.text = "${AppInterface.MOVIE_SPAN_DIFFERENCE}"
        msd?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
        msd?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.MOVIE_SPAN_DIFFERENCE = newValue.toString().toInt()
            return@setOnPreferenceChangeListener true
        }

        val dct = findPreference<EditTextPreference>(DOWNLOAD_CONNECTION_TIMEOUT_PREF)
        dct?.text = "${AppInterface.DOWNLOAD_CONNECTION_TIMEOUT}"
        dct?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
        dct?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.DOWNLOAD_CONNECTION_TIMEOUT = newValue.toString().toInt()
            return@setOnPreferenceChangeListener true
        }

        val dtsPref = findPreference<EditTextPreference>(DOWNLOAD_TIMEOUT_SECOND_PREF)
        dtsPref?.text = "${AppInterface.DOWNLOAD_TIMEOUT_SECOND}"
        dtsPref?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
        dtsPref?.setOnPreferenceChangeListener { _, newValue ->
            AppInterface.DOWNLOAD_TIMEOUT_SECOND = newValue.toString().toInt()
            return@setOnPreferenceChangeListener true
        }
    }
}