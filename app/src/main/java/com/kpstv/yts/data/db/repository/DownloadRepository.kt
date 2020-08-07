package com.kpstv.yts.data.db.repository

import androidx.lifecycle.LiveData
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.data.models.response.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val db: MainDatabase
) {
    private suspend fun getDownload(hash: String): Model.response_download? {
        return withContext(Dispatchers.IO) {
            db.getDownloadDao().getDownload(hash)
        }
    }

    private val lock = Any()
    fun saveDownload(data: Model.response_download) {
        Coroutines.io {
            synchronized(lock) {
                if (db.getDownloadDao().getDownload(data.hash) == null)
                    db.getDownloadDao().upsert(data)
            }
        }
    }

    fun updateDownload(hash: String, recentlyPlayed: Boolean, lastPosition: Int) = Coroutines.io {
        db.getDownloadDao().updateDownload(hash, recentlyPlayed, lastPosition)
    }

    fun deleteDownload(hash: String) {
        Coroutines.io {
            getDownload(hash)?.let {
                db.getDownloadDao().delete(it)
            }
        }
    }

    suspend fun updateAllNormalDownloads() {
        db.getDownloadDao().getAllDownloads().forEach {
            db.getDownloadDao().updateDownload(it.hash, false)
        }
    }

    suspend fun getAllDownloads(): LiveData<List<Model.response_download>> {
        return withContext(Dispatchers.IO) {
            db.getDownloadDao().getAllLiveDownloads()
        }
    }

}