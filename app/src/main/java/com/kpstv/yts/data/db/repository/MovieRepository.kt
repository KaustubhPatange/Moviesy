package com.kpstv.yts.data.db.repository

import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepository(
    private val db: MainDatabase,
    private val ytsPlaceholderApi: YTSPlaceholderApi
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