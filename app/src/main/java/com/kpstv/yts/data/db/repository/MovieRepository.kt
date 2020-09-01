package com.kpstv.yts.data.db.repository

import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.MovieDao
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.interfaces.api.YTSApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val movieDao: MovieDao
) {

    suspend fun getMovieById(movieId: Int) = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)
    }

    suspend fun getCastById(movieId: Int) = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)?.cast
    }

    suspend fun getCrewById(movieId: Int) = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)?.crew
    }

    suspend fun getMovieByTitleLong(queryString: String) =
        withContext(Dispatchers.IO) {
            movieDao.getMovieByTitleLong(queryString)
        }

    fun saveMovie(movie: Movie) =
        Coroutines.io {
            movieDao.upsert(movie)
        }
}