package com.kpstv.yts.data.db.repository

import com.kpstv.yts.data.db.localized.RecommendDatabase
import com.kpstv.yts.data.db.localized.SuggestionDatabase
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.models.data.data_tmdb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TMdbRepository(
    private val suggestDb: SuggestionDatabase,
    private val recommendDb: RecommendDatabase
) {
    suspend fun getSuggestMoviesByIMDB(imdbCode: String): data_tmdb? {
        return withContext(Dispatchers.IO) {
            suggestDb.getTMdbDao().getMovieData(imdbCode)
        }
    }

    fun saveSuggestMoviesModel(data: data_tmdb) {
        Coroutines.io {
            suggestDb.getTMdbDao().upsert(data)
        }
    }

    fun deleteSuggestMovieModel(data: data_tmdb) {
        Coroutines.io {
            suggestDb.getTMdbDao().delete(data)
        }
    }

    suspend fun getRecommendMoviesByIMDB(imdbCode: String): data_tmdb? {
        return withContext(Dispatchers.IO) {
            recommendDb.getTMdbDao().getMovieData(imdbCode)
        }
    }

    fun saveRecommendMoviesModel(data: data_tmdb) {
        Coroutines.io {
            recommendDb.getTMdbDao().upsert(data)
        }
    }

    fun deleteRecommendMovieModel(data: data_tmdb) {
        Coroutines.io {
            recommendDb.getTMdbDao().delete(data)
        }
    }
}