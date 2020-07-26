package com.kpstv.yts.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.AppSettings
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.AppUtils

class AboutSettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preference, rootKey)

        findPreference<Preference>(AppSettings.AUTHOR_OWNER_PREF)?.setOnPreferenceClickListener {
            launch(getString(R.string.author_link))
            true
        }

        findPreference<Preference>(AppSettings.AUTHOR_EMAIL_PREF)?.setOnPreferenceClickListener {
            AppUtils.launchUrlIntent("mailto:${getString(R.string.author_mail)}", requireContext())
            true
        }

        findPreference<Preference>(AppSettings.APP_PACKAGE_PREF)?.summary =
            requireContext().packageName
        findPreference<Preference>(AppSettings.APP_VERSION_PREF)?.summary =
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        findPreference<Preference>(AppSettings.APP_REPORT_PREF)?.setOnPreferenceClickListener {
            AppUtils.launchUrlIntent(getString(R.string.app_github), requireContext())
            true
        }
    }

    private fun launch(url: String) {
        AppUtils.launchUrl(requireContext(), url, IS_DARK_THEME)
    }
}