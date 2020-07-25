package com.kpstv.yts

import android.content.Context
import android.provider.MediaStore
import androidx.preference.PreferenceManager
import java.io.File

object AppSettings {
    fun parseSettings(context: Context) {
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
        AppInterface.IS_DARK_THEME = settingsPref.getBoolean(
            IS_DARK_THEME_PREF,
            AppInterface.IS_DARK_THEME
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
            putBoolean(IS_DARK_THEME_PREF, AppInterface.IS_DARK_THEME)
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
}