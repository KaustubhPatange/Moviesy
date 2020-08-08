package com.kpstv.yts.data.db.repository

import androidx.lifecycle.LiveData
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.DownloadDao
import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.data.models.response.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {
    private suspend fun getDownload(hash: String): Model.response_download? {
        return withContext(Dispatchers.IO) {
            downloadDao.getDownload(hash)
        }
    }

    private val lock = Any()
    fun saveDownload(data: Model.response_download) {
        Coroutines.io {
            synchronized(lock) {
                if (downloadDao.getDownload(data.hash) == null)
                    downloadDao.upsert(data)
            }
        }
    }

    fun updateDownload(hash: String, recentlyPlayed: Boolean, lastPosition: Int) = Coroutines.io {
        downloadDao.updateDownload(hash, recentlyPlayed, lastPosition)
    }

    fun deleteDownload(hash: String) {
        Coroutines.io {
            getDownload(hash)?.let {
                downloadDao.delete(it)
            }
        }
    }

    suspend fun updateAllNormalDownloads() {
        downloadDao.getAllDownloads().forEach {
            downloadDao.updateDownload(it.hash, false)
        }
    }

    suspend fun getAllDownloads(): LiveData<List<Model.response_download>> {
        return withContext(Dispatchers.IO) {
            downloadDao.getAllLiveDownloads()
        }
    }

}