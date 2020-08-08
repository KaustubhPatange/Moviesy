package com.kpstv.yts.data.db.repository

import androidx.lifecycle.LiveData
import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.PauseDao
import com.kpstv.yts.data.models.response.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PauseRepository @Inject constructor(
    private val pauseDao: PauseDao
) {
    private suspend fun getPauseModelByQuery(hash: String): Model.response_pause? {
        return withContext(Dispatchers.IO) {
            pauseDao.getTorrentJob(hash)
        }
    }

    fun savePauseModel(data: Model.response_pause) {
        Coroutines.io {
            if (pauseDao.getTorrentJob(data.hash) == null)
                pauseDao.upsert(data)
        }
    }

    fun deletePause(hash: String) {
        Coroutines.io {
            getPauseModelByQuery(hash)?.let {
                pauseDao.delete(it)
            }
        }
    }

    suspend fun getAllPauseJob(): LiveData<List<Model.response_pause>> {
        return withContext(Dispatchers.IO) {
            pauseDao.getAllData()
        }
    }
}