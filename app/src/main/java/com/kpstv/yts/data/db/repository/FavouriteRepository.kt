package com.kpstv.yts.data.db.repository

import androidx.lifecycle.LiveData
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.FavouriteDao
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.data.models.response.Model
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteRepository @Inject constructor (
    private val favDao: FavouriteDao
) {
    fun getMovieIdByQuery(id: Int): Model.response_favourite? {
        return favDao.getData(id)
    }

    fun saveMovie(data: Model.response_favourite) {
        Coroutines.io {
            favDao.upsert(data)
        }
    }

    fun isMovieFavouriteLive(movieId: Int) = favDao.isDataExistLive(movieId)
    fun isMovieFavourite(movieId: Int) = favDao.isDataExist(movieId)

    /**
     * @return true if current [movie] is marked as favourite
     */
    suspend fun toggleFavourite(movie: Movie): Boolean {
        return if ( favDao.isDataExist(movie.id)) {
            favDao.delete(movie.id)
            false
        }
        else {
            favDao.upsert(
                Model.response_favourite(
                    movieId = movie.id,
                    imdbCode = movie.imdb_code,
                    title = movie.title,
                    imageUrl = movie.medium_cover_image,
                    runtime = movie.runtime,
                    rating = movie.rating,
                    year = movie.year
                )
            )
            true
        }
    }

    fun deleteMovie(movieId: Int) {
        Coroutines.io {
            favDao.delete(movieId)
        }
    }

   fun getAllMovieId(): LiveData<List<Model.response_favourite>> {
        return favDao.getAllData()
    }
}