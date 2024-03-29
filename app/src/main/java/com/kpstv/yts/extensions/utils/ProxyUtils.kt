package com.kpstv.yts.extensions.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppPreference
import com.kpstv.yts.AppSettings
import com.kpstv.yts.data.converters.AppDatabaseConverter
import com.kpstv.yts.data.models.Result
import com.kpstv.yts.extensions.errors.SSLHandshakeException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class ProxyUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val retrofitUtils: RetrofitUtils
) {
    private val TAG = javaClass.simpleName

    /**
     * To make this suspend worker run on non suspendable method
     * we use a callback function.
     */
    fun check(context: CoroutineContext, onComplete: () -> Unit, onError: (Exception) -> Unit): Job {
        val job = SupervisorJob()
        val appScope = CoroutineScope(context)
        CoroutineScope(job + Dispatchers.IO).launch {
            try {
                checkAsync()
                appScope.launch { onComplete.invoke() }
            } catch (e: Exception) {
                appScope.launch { onError.invoke(e) }
            }
        }
        return job
    }


    /**
     * Method will check for updated proxy which are available on app
     * database.
     */
    private suspend fun checkAsync() {
        Log.e(TAG, "Checking for proxy")
        val result = retrofitUtils.makeHttpCallAsync(AppInterface.APP_DATABASE_URL)
        if (result is Result.Success) {
            val response = result.data
            if (response.isSuccessful) {
                val appDatabase =
                    AppDatabaseConverter.toAppDatabaseFromString(response.body?.string())
                response.close() // Close response for memory leaks
                appDatabase?.yts?.let { yts ->
                    AppInterface.YTS_BASE_URL = yts.base
                    AppInterface.YTS_BASE_API_URL = "${AppInterface.YTS_BASE_URL}/api/v2/"
                    AppInterface.YIFY_BASE_URL = yts.yify
                }
                appDatabase?.tmdb?.let { tmdb ->
                    AppInterface.TMDB_BASE_URL = tmdb.base
                    AppInterface.TMDB_IMAGE_PREFIX = tmdb.image
                    AppInterface.TMDB_API_KEY = tmdb.apiKey
                }
                appDatabase?.misc?.let { misc ->
                    AppInterface.SUGGESTION_URL = misc.suggestionApi
                    AppInterface.EASTER_EGG_URL = misc.easterEggUri
                }

                AppSettings.writeSettings(context)

                Log.e(TAG, "Checking for YTS proxy again!")
                val testResponse = retrofitUtils.makeHttpCallAsync("${AppInterface.YTS_BASE_API_URL}list_movies.json").getOrNull() ?: return

                if (testResponse.code == 525) throw SSLHandshakeException("The app couldn't connect to server")
                if (!testResponse.isSuccessful) throw Exception("Updated proxy is invalid. If you see this message, contact developer to update proxies manually!")
                testResponse.close() // Close response for memory leaks

                AppPreference(context).setShouldCheckProxy(false)

                return
            }
        }

        throw Exception("Failed to retrieve app database")
    }
}