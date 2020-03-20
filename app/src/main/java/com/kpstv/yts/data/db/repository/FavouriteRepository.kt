package com.kpstv.yts.data.db.repository

import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.models.response.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavouriteRepository (
    private val db: MainDatabase
) {
    suspend fun getMovieIdByQuery(id: Int): Model.response_favourite? {
        return withContext(Dispatchers.IO) {
            db.getFavDao().getData(id)
        }
    }

    fun saveMovie(data: Model.response_favourite) {
        Coroutines.io {
            db.getFavDao().upsert(data)
        }
    }

    fun deleteMovie(movieId: Int) {
        Coroutines.io {
            getMovieIdByQuery(movieId)?.let {
                db.getFavDao().delete(it)
            }
        }
    }

    suspend fun getAllMovieId(): List<Model.response_favourite> {
        return withContext(Dispatchers.IO) {
            db.getFavDao().getAllData()
        }
    }

}