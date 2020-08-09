package com.kpstv.yts.data.db.repository

import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.HistoryDao
import com.kpstv.yts.data.models.data.data_history
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun insert(data: data_history) = Coroutines.io {
        historyDao.getData(data.query)?.let { historyDao.delete(it) }
        historyDao.insert(data)
    }

    fun remove(query: String) = Coroutines.io {
        historyDao.getData(query)?.let { historyDao.delete(it) }
    }

    suspend fun getRecentHistory(max: Int) = historyDao.getAllData(max)
}