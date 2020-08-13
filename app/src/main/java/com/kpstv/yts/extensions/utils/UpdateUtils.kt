package com.kpstv.yts.extensions.utils

import android.content.Context
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.data.converters.AppDatabaseConverter
import com.kpstv.yts.data.models.AppDatabase
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.services.UpdateWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val retrofitUtils: RetrofitUtils
) {

    /**
     * To make this suspend worker run on non suspendable method
     * we use a callback function.
     */
    fun check(
        onUpdateAvailable: (AppDatabase.Update) -> Unit,
        onUpdateNotFound: SimpleCallback? = null,
        onError: (Exception) -> Unit
    ) =
        Coroutines.io {
            try {
                val updatePair = fetchUpdateDetails()
                if (updatePair.second) {
                    Coroutines.main { onUpdateAvailable.invoke(updatePair.first.update) }
                } else Coroutines.main { onUpdateNotFound?.invoke() }
            } catch (e: Exception) {
                Coroutines.main { onError(e) }
            }
        }

    suspend fun checkAsync(): Pair<AppDatabase.Update, Boolean> {
        val details = fetchUpdateDetails()
        return Pair(details.first.update, details.second)
    }

    fun processUpdate(update: AppDatabase.Update) = with(context) {
        UpdateWorker.schedule(applicationContext, update.url)
    }

    private suspend fun fetchUpdateDetails(): Pair<AppDatabase, Boolean> {
        val response = retrofitUtils.makeHttpCallAsync(AppInterface.APP_DATABASE_URL)
        if (response.isSuccessful) {
            val appDatabase = AppDatabaseConverter
                .toAppDatabaseFromString(response.body?.string())
            response.close()
            if (appDatabase == null) throw Exception("Failed to obtain details from the response")
            return Pair(appDatabase, appDatabase.update.versionCode > BuildConfig.VERSION_CODE)
        } else
            throw Exception("Failed to retrieve app database")
    }
}