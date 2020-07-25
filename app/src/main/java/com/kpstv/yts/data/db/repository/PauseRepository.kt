package com.kpstv.yts.data.db.repository

import androidx.lifecycle.LiveData
import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.data.models.response.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PauseRepository @Inject constructor(
    val db: MainDatabase
) {
    private suspend fun getPauseModelByQuery(hash: String): Model.response_pause? {
        return withContext(Dispatchers.IO) {
            db.getPauseDao().getTorrentJob(hash)
        }
    }

    fun savePauseModel(data: Model.response_pause) {
        Coroutines.io {
            db.getPauseDao().upsert(data)
        }
    }

    fun deletePause(hash: String) {
        Coroutines.io {
            getPauseModelByQuery(hash)?.let {
                db.getPauseDao().delete(it)
            }
        }
    }

    suspend fun getAllPauseJob(): LiveData<List<Model.response_pause>> {
        return withContext(Dispatchers.IO) {
            db.getPauseDao().getAllData()
        }
    }
}