package com.kpstv.yts.data.db.repository

import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.MainDao
import com.kpstv.yts.data.models.data.data_main
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val mainDao: MainDao
) {

    suspend fun getMoviesByQuery(queryString: String): data_main? {
        return withContext(Dispatchers.IO) {
            mainDao.getMovies(queryString)
        }
    }

    fun removeMoviesByQuery(queryString: String) {
        mainDao.getMovies(queryString)?.let {
            deleteMovies(it)
        }
    }

    fun saveMovies(dataMain: data_main) {
        Coroutines.io {
            mainDao.upsert(dataMain)
        }
    }

    fun deleteMovies(dataMain: data_main) {
        Coroutines.io {
            mainDao.delete(dataMain)
        }
    }
}