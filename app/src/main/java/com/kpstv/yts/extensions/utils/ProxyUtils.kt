package com.kpstv.yts.extensions.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings
import com.kpstv.yts.data.converters.AppDatabaseConverter
import com.kpstv.yts.extensions.Coroutines
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProxyUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val retrofitUtils: RetrofitUtils
) {
    private val TAG = javaClass.simpleName
    /**
     * Method will check for updated proxy which are available on app
     * database.
     */
    suspend fun checkAsync() {
        Log.e(TAG, "Checking for proxy")
        val response =
            retrofitUtils.getHttpClient()
                .newCall(Request.Builder().url(AppInterface.APP_DATABASE_URL).build()).await()
        if (response.isSuccessful) {
            val appDatabase =
                AppDatabaseConverter.toAppDatabaseFromString(response.body?.string())
            appDatabase?.yts?.let { yts ->
                AppInterface.YTS_BASE_URL = yts.base
                AppInterface.YIFY_BASE_URL = yts.yify
            }
            appDatabase?.tmdb?.let { tmdb ->
                AppInterface.TMDB_BASE_URL = tmdb.base
                AppInterface.TMDB_IMAGE_PREFIX = tmdb.image
                AppInterface.TMDB_API_KEY = tmdb.apiKey
            }
            appDatabase?.misc?.let { misc ->
                AppInterface.SUGGESTION_URL = misc.suggestionApi
            }

            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putBoolean(AppInterface.PROXY_CHECK_PREF, false)
                putBoolean(AppInterface.IS_FIRST_LAUNCH_PREF, false)
            }

            AppSettings.writeSettings(context)
        }
    }

    /**
     * To make this suspend worker run on non suspendable method
     * we use a callback function.
     */
    fun check(onComplete: () -> Unit) = Coroutines.io {
        checkAsync()
        Coroutines.main { onComplete.invoke() }
    }
}