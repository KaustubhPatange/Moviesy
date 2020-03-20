package com.kpstv.yts.data.db.repository

import com.kpstv.yts.data.db.localized.MainDatabase
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.models.response.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadRepository  (
    private val db: MainDatabase
) {
    suspend fun getDownload(hash: String): Model.response_download? {
        return withContext(Dispatchers.IO) {
            db.getDownloadDao().getDownload(hash)
        }
    }

    fun saveDownload(data: Model.response_download) {
        Coroutines.io {
            db.getDownloadDao().upsert(data)
        }
    }

    fun deleteDownload(hash: String) {
        Coroutines.io {
            getDownload(hash)?.let {
                db.getDownloadDao().delete(it)
            }
        }
    }

    suspend fun getAllDownloads(): List<Model.response_download> {
        return withContext(Dispatchers.IO) {
            db.getDownloadDao().getAllDownloads()
        }
    }

}