package com.kpstv.yts.data.db.repository

import androidx.lifecycle.LiveData
import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.models.response.Model
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteRepository @Inject constructor (
    private val db: MainDatabase
) {
    fun getMovieIdByQuery(id: Int): Model.response_favourite? {
        return db.getFavDao().getData(id)
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

   fun getAllMovieId(): LiveData<List<Model.response_favourite>> {
        return db.getFavDao().getAllData()
    }
}