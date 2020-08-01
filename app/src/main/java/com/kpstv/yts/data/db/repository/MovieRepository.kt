package com.kpstv.yts.data.db.repository

import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.data.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val db: MainDatabase,
    private val ytsApi: YTSApi
) {

    suspend fun getMovieById(movieId: Int) : Movie? {
        return withContext(Dispatchers.IO) {
            db.getMovieDao().getMovieById(movieId)
        }
    }

    suspend fun getMovieByTitleLong(queryString: String) : Movie? {
        return withContext(Dispatchers.IO) {
            db.getMovieDao().getMovieByTitleLong(queryString)
        }
    }

    fun saveMovie(movie: Movie) =
        Coroutines.io {
            db.getMovieDao().upsert(movie)
        }
}