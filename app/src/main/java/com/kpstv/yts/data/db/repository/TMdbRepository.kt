package com.kpstv.yts.data.db.repository

import android.util.Log
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.db.localized.RecommendDao
import com.kpstv.yts.data.db.localized.SuggestionDao
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.data.data_tmdb
import com.kpstv.yts.extensions.MovieType
import com.kpstv.yts.extensions.SuggestionCallback
import com.kpstv.yts.interfaces.api.TMdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TMdbRepository @Inject constructor(
    private val suggestDao: SuggestionDao,
    private val recommendDao: RecommendDao,
    private val tMdbApi: TMdbApi
) {
    private val TAG = javaClass.simpleName

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