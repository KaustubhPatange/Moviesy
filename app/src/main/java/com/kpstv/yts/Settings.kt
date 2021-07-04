package com.kpstv.yts

import android.content.Context
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.kpstv.yts.ui.helpers.ThemeHelper
import java.io.File

class AppPreference(context: Context) {
    companion object {
        private const val SHOULD_CHECK_PROXY = "should_check_proxy"
        private const val IS_FIRST_LAUNCH = "is_first_launch"
        private const val THEME_SETTING = "theme_setting"
        private const val VPN_HELP_VIEW = "vpn_help_view"
    }

    private val pm = PreferenceManager.getDefaultSharedPreferences(context)

    fun shouldCheckProxy(): Boolean = getBoolean(SHOULD_CHECK_PROXY, false)

    fun setShouldCheckProxy(value: Boolean) {
        writeBoolean(SHOULD_CHECK_PROXY, value)
    }

    fun isFirstLaunch(): Boolean = getBoolean(IS_FIRST_LAUNCH, true)

    fun isFirstLaunch(value: Boolean) = writeBoolean(IS_FIRST_LAUNCH, value)

    fun getBoolean(key: String, default: Boolean) = pm.getBoolean(key, default)

    fun writeBoolean(key: String, value: Boolean) = pm.edit {
        putBoolean(key, value)
    }

    fun setTheme(theme: ThemeHelper.AppTheme) {
        pm.edit { putString(THEME_SETTING, theme.name) }
        ThemeHelper.updateValues(this)
    }

    fun getTheme() : ThemeHelper.AppTheme =
        ThemeHelper.AppTheme.valueOf(pm.getString(THEME_SETTING, ThemeHelper.AppTheme.DARK.toString())!!)

    fun isVpnHelpShown(): Boolean = getBoolean(VPN_HELP_VIEW, false)
    fun setVpnHelpShown(value: Boolean) = writeBoolean(VPN_HELP_VIEW, value)
}

fun Context.defaultPreference() =
    lazy { AppPreference(this) }

fun Fragment.defaultPreference() =
    lazy { AppPreference(requireContext()) }

object AppSettings {
    fun parseSettings(context: Context) {
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)

        AppInterface.IS_PREMIUM_UNLOCKED = settingsPref.getBoolean(
            PREMIUM_PURCHASE_PREF,
            AppInterface.IS_PREMIUM_UNLOCKED
        )

        AppInterface.TMDB_IMAGE_PREFIX = settingsPref.getString(
            TMDB_IMAGE_PREFIX_PREF,
            AppInterface.TMDB_IMAGE_PREFIX
        )!!

        AppInterface.COUNTRY_FLAG_JSON_URL = settingsPref.getString(
            COUNTRY_FLAG_JSON_URL_PREF,
            AppInterface.COUNTRY_FLAG_JSON_URL
        )!!

        AppInterface.SUGGESTION_URL = settingsPref.getString(
            SUGGESTION_URL_PREF,
            AppInterface.SUGGESTION_URL
        )!!

        AppInterface.TMDB_API_KEY = settingsPref.getString(
            TMDB_API_KEY_PREF,
            AppInterface.TMDB_API_KEY
        )!!

        AppInterface.DOWNLOAD_TIMEOUT_SECOND = settingsPref.getString(
            DOWNLOAD_TIMEOUT_SECOND_PREF,
            AppInterface.DOWNLOAD_TIMEOUT_SECOND.toString()
        )!!.toInt()

        AppInterface.DOWNLOAD_CONNECTION_TIMEOUT = settingsPref.getString(
            DOWNLOAD_CONNECTION_TIMEOUT_PREF,
            AppInterface.DOWNLOAD_CONNECTION_TIMEOUT.toString()
        )!!.toInt()

        AppInterface.MOVIE_SPAN_DIFFERENCE = settingsPref.getString(
            MOVIE_SPAN_DIFFERENCE_PREF,
            AppInterface.MOVIE_SPAN_DIFFERENCE.toString()
        )!!.toInt()

        AppInterface.QUERY_SPAN_DIFFERENCE = settingsPref.getString(
            QUERY_SPAN_DIFFERENCE_PREF,
            AppInterface.QUERY_SPAN_DIFFERENCE.toString()
        )!!.toInt()

        AppInterface.CUSTOM_LAYOUT_YTS_SPAN = settingsPref.getString(
            CUSTOM_LAYOUT_YTS_SPAN_PREF,
            AppInterface.CUSTOM_LAYOUT_YTS_SPAN.toString()
        )!!.toInt()

        AppInterface.YTS_BASE_URL =
            settingsPref.getString(YTS_BASE_URL_PREF, AppInterface.YTS_BASE_URL)!!

        AppInterface.YIFY_BASE_URL = settingsPref.getString(
            YIFY_BASE_URL_PREF,
            AppInterface.YIFY_BASE_URL
        )!!

        AppInterface.TMDB_BASE_URL = settingsPref.getString(
            TMDB_BASE_URL_PREF,
            AppInterface.TMDB_BASE_URL
        )!!

        AppInterface.ANONYMOUS_TORRENT_DOWNLOAD = settingsPref.getBoolean(
            ANONYMOUS_TORRENT_DOWNLOAD_PREF,
            AppInterface.ANONYMOUS_TORRENT_DOWNLOAD
        )

        val downloadFile = settingsPref.getString(
            STORAGE_LOCATION_PREF,
            AppInterface.STORAGE_LOCATION.path
        )!!
        AppInterface.STORAGE_LOCATION = File(downloadFile)
    }

    fun writeSettings(context: Context) {
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
        settingsPref.edit().apply {
            putBoolean(PREMIUM_PURCHASE_PREF, AppInterface.IS_PREMIUM_UNLOCKED)
            putString(TMDB_IMAGE_PREFIX_PREF, AppInterface.TMDB_IMAGE_PREFIX)
            putString(STORAGE_LOCATION_PREF, AppInterface.STORAGE_LOCATION.path)
            putString(COUNTRY_FLAG_JSON_URL_PREF, AppInterface.COUNTRY_FLAG_JSON_URL)
            putString(SUGGESTION_URL_PREF, AppInterface.SUGGESTION_URL)
            putString(TMDB_API_KEY_PREF, AppInterface.TMDB_API_KEY)
            putString(DOWNLOAD_TIMEOUT_SECOND_PREF, AppInterface.DOWNLOAD_TIMEOUT_SECOND.toString())
            putString(
                DOWNLOAD_CONNECTION_TIMEOUT_PREF,
                AppInterface.DOWNLOAD_CONNECTION_TIMEOUT.toString()
            )
            putString(QUERY_SPAN_DIFFERENCE_PREF, AppInterface.QUERY_SPAN_DIFFERENCE.toString())
            putString(CUSTOM_LAYOUT_YTS_SPAN_PREF, AppInterface.CUSTOM_LAYOUT_YTS_SPAN.toString())
            putString(YTS_BASE_URL_PREF, AppInterface.YTS_BASE_URL)
            putString(YIFY_BASE_URL_PREF, AppInterface.YIFY_BASE_URL)
            putString(TMDB_BASE_URL_PREF, AppInterface.TMDB_BASE_URL)
            putBoolean(ANONYMOUS_TORRENT_DOWNLOAD_PREF, AppInterface.ANONYMOUS_TORRENT_DOWNLOAD)
        }.apply()
    }

    const val TMDB_IMAGE_PREFIX_PREF = "tmdb_image_prefix_pref"
    const val IS_DARK_THEME_PREF = "is_dark_theme_pref"
    const val COUNTRY_FLAG_JSON_URL_PREF = "country_flag_json_url_pref"
    const val SUGGESTION_URL_PREF = "suggestion_url_pref"
    const val TMDB_API_KEY_PREF = "tmdb_api_key_pref"
    const val DOWNLOAD_TIMEOUT_SECOND_PREF = "download_timeout_second_pref"
    const val DOWNLOAD_CONNECTION_TIMEOUT_PREF = "download_connection_timeout_pref"
    const val MOVIE_SPAN_DIFFERENCE_PREF = "movie_span_difference_pref"
    const val QUERY_SPAN_DIFFERENCE_PREF = "query_span_difference_pref"
    const val CUSTOM_LAYOUT_YTS_SPAN_PREF = "custom_layout_yts_span_pref"
    const val YTS_BASE_URL_PREF = "yts_base_url_pref"
    const val YIFY_BASE_URL_PREF = "yify_base_url_perf"
    const val TMDB_BASE_URL_PREF = "tmdb_base_url_pref"
    const val ANONYMOUS_TORRENT_DOWNLOAD_PREF = "anonymous_torrent_download_pref"
    const val STORAGE_LOCATION_PREF = "storage_location_pref"

    const val SHOW_ACCOUNT_ID_PREF = "show_account_id_pref"
    const val PURCHASE_ACCOUNT_ERROR_PREF = "purchase_account_error_pref"

    const val AUTHOR_OWNER_PREF = "author_owner_pref"
    const val AUTHOR_EMAIL_PREF = "author_email_pref"
    const val APP_PACKAGE_PREF = "app_package_pref"
    const val APP_VERSION_PREF = "app_version_pref"
    const val APP_REPORT_PREF = "app_report_pref"

    const val PREMIUM_PURCHASE_PREF = "premium_purchase_pref"
    const val SHOW_DOWNLOAD_TIP_PREF = "show_download_tip_pref"
}