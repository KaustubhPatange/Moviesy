package com.kpstv.yts.data.db.repository

import android.util.Log
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.adapters.HistoryModel
import com.kpstv.yts.data.db.localized.HistoryDao
import com.kpstv.yts.data.models.Result
import com.kpstv.yts.data.models.data.data_history
import com.kpstv.yts.extensions.SearchResults
import com.kpstv.yts.extensions.SearchType
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.interfaces.api.TMdbApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao,
    private val retrofitUtils: RetrofitUtils,
    private val tMdbApi: TMdbApi
) {
    private var previousSearchResult: String? = null

    fun insert(data: data_history) = Coroutines.io {
        historyDao.getData(data.query)?.let { historyDao.delete(it) }
        historyDao.insert(data)
    }

    fun remove(query: String) = Coroutines.io {
        historyDao.getData(query)?.let { historyDao.delete(it) }
    }

    private suspend fun getRecentHistory(max: Int) = historyDao.getAllData(max)

    fun getSearchResults(text: String, type: SearchType): Flow<Result<SearchResults>> = flow {
        if (text == previousSearchResult) return@flow
        previousSearchResult = text
        Log.e(javaClass.simpleName, "Processing text: $text")
        /** If search text is empty show history or else return empty */
        if (text.isEmpty()) {
            val history = getRecentHistory(5)
            if (history.isEmpty())
                emit(Result.empty())
            else
                emit(Result.success(history.map {
                    HistoryModel(
                        query = it.query,
                        type = HistoryModel.Type.HISTORY
                    )
                }))
        } else {
            /** Search for suggestion based on types */
            try {
                when (type) {
                    SearchType.Google -> {
                        val list = getGoogleSuggestions(text).map {
                            HistoryModel(
                                query = it,
                                type = HistoryModel.Type.SEARCH
                            )
                        }
                       emit(Result.success(list))
                    }
                    SearchType.TMDB -> {
                        val list = getTMDBSuggestions(text).map {
                            HistoryModel(
                                query = it,
                                type = HistoryModel.Type.SEARCH
                            )
                        }
                        emit(Result.success(list))
                    }
                }
            } catch (e: Exception) {
                emit(Result.empty())
            }
        }
    }

    private suspend fun getGoogleSuggestions(text: String): List<String> {
        val list = ArrayList<String>()

        val result = retrofitUtils.makeHttpCallAsync("${AppInterface.SUGGESTION_URL}$text")
        if (result is Result.Success) {
            val response = result.data
            
            val json = response.body?.string()
            response.body?.close() // Always close the stream

            if (json?.isNotEmpty() == true) {
                val jsonArray = JSONArray(json).getJSONArray(1)
                for (i in 0 until jsonArray.length())
                    list.add(jsonArray.getString(i))
            }
        }

        return list
    }

    private suspend fun getTMDBSuggestions(text: CharSequence): List<String> {
        val results = tMdbApi.getSearch(text.toString())
        return results.results.map { it.original_title }
    }
}