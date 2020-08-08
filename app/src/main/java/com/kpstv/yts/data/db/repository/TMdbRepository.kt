package com.kpstv.yts.data.db.repository

import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.localized.RecommendDao
import com.kpstv.yts.data.db.localized.SuggestionDao
import com.kpstv.yts.data.models.data.data_tmdb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TMdbRepository @Inject constructor(
    private val suggestDao: SuggestionDao,
    private val recommendDao: RecommendDao
) {
    suspend fun getSuggestMoviesByIMDB(imdbCode: String): data_tmdb? {
        return withContext(Dispatchers.IO) {
            suggestDao.getMovieData(imdbCode)
        }
    }

    fun saveSuggestMoviesModel(data: data_tmdb) {
        Coroutines.io {
            suggestDao.upsert(data)
        }
    }

    fun deleteSuggestMovieModel(data: data_tmdb) {
        Coroutines.io {
            suggestDao.delete(data)
        }
    }

    suspend fun getRecommendMoviesByIMDB(imdbCode: String): data_tmdb? {
        return withContext(Dispatchers.IO) {
            recommendDao.getMovieData(imdbCode)
        }
    }

    fun saveRecommendMoviesModel(data: data_tmdb) {
        Coroutines.io {
            recommendDao.upsert(data)
        }
    }

    fun deleteRecommendMovieModel(data: data_tmdb) {
        Coroutines.io {
            recommendDao.delete(data)
        }
    }
}