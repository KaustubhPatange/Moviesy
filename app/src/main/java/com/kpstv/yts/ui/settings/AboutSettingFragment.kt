package com.kpstv.yts.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.helpers.ThemeHelper
import kotlin.math.abs

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

        findPreference<Preference>(AppSettings.APP_REPORT_PREF)?.setOnPreferenceClickListener {
            launch(getString(R.string.app_github))
            true
        }

        findPreference<Preference>(AppSettings.APP_VERSION_PREF)?.apply {
            summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            var count = 0
            val toast = Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT)
            setOnPreferenceClickListener {
                if (count == EASTER_EGG_CLICK_COUNT - 1) {
                    AppUtils.launchUrlIntent(requireContext(), AppInterface.EASTER_EGG_URL)
                    count = 0
                } else {
                    count++
                    val reminder = abs(EASTER_EGG_CLICK_COUNT - count)
                    if (reminder <= 3) {
                        toast.setText("Waiting for easter egg, $reminder count")
                        toast.show()
                    }
                }
                true
            }
        }
    }

    private fun launch(url: String) {
        AppUtils.launchUrl(requireContext(), url, ThemeHelper.isDarkVariantTheme())
    }

    private companion object {
        const val EASTER_EGG_CLICK_COUNT = 8
    }
}