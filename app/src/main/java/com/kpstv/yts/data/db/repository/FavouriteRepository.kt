package com.kpstv.yts.data.db.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.models.response.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class FavouriteRepository (
    private val db: MainDatabase
) {

  //  private val movieIds = MutableLiveData<List<Model.response_favourite>>()
/*
    init {
        movieIds.observeForever {
            saveAllMovieId(it)
        }
    }*/

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

    private fun saveAllMovieId(quotes: List<Model.response_favourite>) {
        Coroutines.io {
            db.getFavDao().saveAllData(quotes)
        }
    }

    fun deleteMovie(movieId: Int) {
        Coroutines.io {
            getMovieIdByQuery(movieId)?.let {
                db.getFavDao().delete(it)
            }
        }
    }

    suspend fun getAllMovieId(): LiveData<List<Model.response_favourite>> {
        return withContext(Dispatchers.IO) {
            db.getFavDao().getAllData()
        }
    }

}