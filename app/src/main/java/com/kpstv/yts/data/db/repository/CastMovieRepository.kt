package com.kpstv.yts.data.db.repository

import android.util.Log
import com.kpstv.yts.data.db.localized.TmDbCastDao
import com.kpstv.yts.data.db.localized.TmDbCastMovieDao
import com.kpstv.yts.data.models.TmDbCast
import com.kpstv.yts.data.models.TmDbCastDomain
import com.kpstv.yts.data.models.TmDbCastMovie
import com.kpstv.yts.interfaces.api.TMdbApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastMovieRepository @Inject constructor(
    private val tMdbApi: TMdbApi,
    private val tmDbCastDao: TmDbCastDao,
    private val tmDbCastMovieDao: TmDbCastMovieDao
) {
    /**
     * @return list of castMovies
     */
    suspend fun fetchResults(imdbCode: String): List<TmDbCastMovie> {
        val results = fetchFromLocal(imdbCode)
        return if (results.isEmpty())
            fetchFromRemote(imdbCode)
        else results
    }

    private suspend fun fetchFromLocal(imdbCode: String): List<TmDbCastMovie> {
        val list = ArrayList<TmDbCastMovie>()

        tmDbCastDao.getCasts(imdbCode).forEach { tmDbCastDomain ->
            tmDbCastMovieDao.getMovies(tmDbCastDomain.cast.personId)?.let { list.add(it) }
        }

        return list
    }

    private suspend fun fetchFromRemote(imdbCode: String): List<TmDbCastMovie> {
        val casts = tMdbApi.getCast(imdbCode).cast?.take(5) ?: return emptyList()

        val femaleCast = casts.find { it.gender == 1 } ?: casts.first() // Get a female cast
        val maleCast = casts.find { it.gender == 2 } ?: casts.first() // Get a male cast

        return if (maleCast.personId != femaleCast.personId) {
            tmDbCastDao.insert(TmDbCastDomain(imdbCode = imdbCode, cast = femaleCast))
            tmDbCastDao.insert(TmDbCastDomain(imdbCode = imdbCode, cast = maleCast))

            val maleRemote = fetchCastMovies(maleCast)
            val femaleRemote = fetchCastMovies(femaleCast)

            insertToCastMovieDao(maleRemote)
            insertToCastMovieDao(femaleRemote)

            listOf(maleRemote, femaleRemote)
        } else {
            tmDbCastDao.insert(TmDbCastDomain(imdbCode = imdbCode, cast = maleCast))

            val maleRemote = fetchCastMovies(maleCast)

            insertToCastMovieDao(maleRemote)

            listOf(maleRemote)
        }
    }

    private suspend fun fetchCastMovies(cast: TmDbCast): TmDbCastMovie {
        val movies =
            tMdbApi.getPersonCredits(cast.personId).cast.sortedByDescending { it.popularity }
        Log.e(javaClass.simpleName, "Movie size: ${movies.size}")
        return if (movies.size > 10)
            TmDbCastMovie.from(cast, movies.take(10))
        else TmDbCastMovie.from(cast, movies)
    }

    private suspend fun insertToCastMovieDao(data: TmDbCastMovie) {
        if (tmDbCastMovieDao.getMovies(data.personId) == null)
            tmDbCastMovieDao.insert(data)
    }
}