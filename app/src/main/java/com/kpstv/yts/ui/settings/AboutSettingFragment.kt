package com.kpstv.yts.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.yts.AppSettings
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.helpers.ThemeHelper

class AboutSettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preference, rootKey)

        findPreference<Preference>(AppSettings.AUTHOR_OWNER_PREF)?.setOnPreferenceClickListener {
            launch(getString(R.string.author_link))
            true
        }

        findPreference<Preference>(AppSettings.AUTHOR_EMAIL_PREF)?.setOnPreferenceClickListener {
            AppUtils.launchUrlIntent(requireContext(), "mailto:${getString(R.string.author_mail)}")
            true
        }

        findPreference<Preference>(AppSettings.APP_PACKAGE_PREF)?.summary =
            requireContext().packageName
        findPreference<Preference>(AppSettings.APP_VERSION_PREF)?.summary =
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        findPreference<Preference>(AppSettings.APP_REPORT_PREF)?.setOnPreferenceClickListener {
            launch(getString(R.string.app_github))
            true
        }
    }

    private fun launch(url: String) {
        AppUtils.launchUrl(requireContext(), url, ThemeHelper.isDarkVariantTheme())
    }
}