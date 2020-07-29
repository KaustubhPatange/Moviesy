package com.kpstv.yts.data

import android.content.Context
import androidx.paging.PageKeyedDataSource
import com.kpstv.yts.AppInterface.Companion.TMDB_IMAGE_PREFIX
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.response.Model
import es.dmoral.toasty.Toasty
import retrofit2.await

/** Using pagination architecture to perform endless recyclerView scrolling
 *  to show movies
 */

class CustomDataSource(
    private val context: Context,
    private val tMdbApi: TMdbApi,
    private val ytsApi: YTSApi
) : PageKeyedDataSource<Int, MovieShort>() {

    companion object {
        var INITIAL_QUERY_FETCHED = false
    }

    private val TAG = "CustomDataSource"
    private var endPoint: String? = null
    private var base: MovieBase? = null
    private var queryMap: Map<String, String>? = null

    private val FIRST_PAGE = 1;

    /** This will handle TMDB movie queries
     */
    fun setTMdbMovieSource(endPoint: String?, base: MovieBase?) {
        this.endPoint = endPoint
        this.base = base
    }

    /** This will handle YTS movie queries
     */
    fun setYtsMovieSource(queryMap: Map<String, String>?, base: MovieBase?) {
        this.queryMap = queryMap
        this.base = base
    }

    private suspend fun executeTMdbQuery(page: Int): Model.response_tmdb_movies? {
        val split = endPoint?.split("/")
        if (split != null) {
            if (endPoint?.contains("/similar") == true) {
                return tMdbApi.getSimilars(split[0], page).await()
            } else if (endPoint?.contains("/recommendations") == true) {
                return tMdbApi.getRecommendations(split[0].toInt(), page).await()
            }
        }
        return null
    }

    @JvmName("createMovieShort1")
    private fun createMovieShort(it: ArrayList<TmDbMovie>): ArrayList<MovieShort> {
        val list = ArrayList<MovieShort>()
        it.forEach { movie ->
            if (movie.release_date?.contains("-") == true) {
                list.add(
                    MovieShort(
                        movieId = movie.id.toInt(),
                        title = movie.title,
                        rating = movie.rating,
                        year = movie.release_date.split("-")[0].toInt(),
                        bannerUrl = "${TMDB_IMAGE_PREFIX}${movie.bannerPath}",
                        runtime = movie.runtime
                    )
                )
            }
        }
        return list
    }

    private suspend fun executeYTSQuery(page: Int): Model.response_movie? {
        return ytsApi.listMovies(queryMap!!, page).await()
    }

    @JvmName("createMovieShort")
    private fun createMovieShort(it: ArrayList<Movie>): ArrayList<MovieShort> {
        val list = ArrayList<MovieShort>()
        it.forEach { movie ->
            list.add(
                MovieShort(
                    movie.id, movie.url, movie.title, movie.year, movie.rating,
                    movie.runtime, movie.imdb_code, movie.medium_cover_image
                )
            )
        }
        return list
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, MovieShort>
    ) {
        INITIAL_QUERY_FETCHED = false
        Coroutines.main {
            try {
                when (base) {
                    MovieBase.TMDB -> {
                        val response = executeTMdbQuery(FIRST_PAGE)
                        response?.let {
                            callback.onResult(createMovieShort(it.results), null, FIRST_PAGE + 1)
                        }
                    }
                    MovieBase.YTS -> {
                        val response = executeYTSQuery(FIRST_PAGE)
                        response?.data?.movies?.let {
                            callback.onResult(createMovieShort(it),null,FIRST_PAGE + 1)
                        }
                    }
                }
                INITIAL_QUERY_FETCHED = true
            } catch (e: Exception) {
                Toasty.error(context, "Error: ${e.message}").show()
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, MovieShort>) {
        try {
            Coroutines.main {
                when (base) {
                    MovieBase.TMDB -> {
                        val response = executeTMdbQuery(params.key)
                        response?.let {
                            val key = if (params.key > 1) params.key - 1 else null
                            callback.onResult(createMovieShort(it.results), key)
                        }
                    }
                    MovieBase.YTS -> {
                        val response = executeYTSQuery(params.key)
                        response?.data?.movies?.let {
                            val key = if (params.key > 1) params.key - 1 else null
                            callback.onResult(createMovieShort(it),key)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toasty.error(context, "Error: ${e.message}").show()
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, MovieShort>) {
        try {
            Coroutines.main {
                when (base) {
                    MovieBase.TMDB -> {
                        val response = executeTMdbQuery(params.key)
                        response?.let {
                            val key =
                                if (response.page != response.total_pages) params.key + 1 else null
                            callback.onResult(createMovieShort(it.results), key)
                        }
                    }
                    MovieBase.YTS -> {
                        val response = executeYTSQuery(params.key)
                        response?.data?.movies?.let {
                            val key =
                                if ((response.data.movie_count/response.data.limit) != response.data.page_number)
                                    params.key +1 else null
                            callback.onResult(createMovieShort(it),key)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toasty.error(context, "Error: ${e.message}").show()
        }
    }
}