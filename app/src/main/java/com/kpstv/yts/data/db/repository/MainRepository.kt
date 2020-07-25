package com.kpstv.yts.data.db.repository

import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.data.models.data.data_main
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val db: MainDatabase,
    private val ytsApi: YTSApi
) {

    suspend fun getMoviesByQuery(queryString: String): data_main? {
        return withContext(Dispatchers.IO) {
            db.getMainDao().getMovies(queryString)
        }
    }

    fun removeMoviesByQuery(queryString: String) {
        deleteMovies(db.getMainDao().getMovies(queryString))
    }

    fun saveMovies(dataMain: data_main) {
        Coroutines.io {
            db.getMainDao().upsert(dataMain)
        }
    }

    fun deleteMovies(dataMain: data_main) {
        Coroutines.io {
            db.getMainDao().delete(dataMain)
        }
    }
}