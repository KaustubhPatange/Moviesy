package com.kpstv.yts.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.AndroidViewModel
import co.metalab.asyncawait.async
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.CUSTOM_LAYOUT_YTS_SPAN
import com.kpstv.yts.AppInterface.Companion.MainDateFormatter
import com.kpstv.yts.AppInterface.Companion.QUERY_SPAN_DIFFERENCE
import com.kpstv.yts.AppInterface.Companion.getPopularUtils
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.data.db.repository.DownloadRepository
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.data.db.repository.MainRepository
import com.kpstv.yts.data.db.repository.PauseRepository
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.extensions.lazyDeferred
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.interfaces.listener.FavouriteListener
import com.kpstv.yts.interfaces.listener.MoviesListener
import com.kpstv.yts.interfaces.listener.ObservableListener
import com.kpstv.yts.models.Movie
import com.kpstv.yts.models.MovieShort
import com.kpstv.yts.models.data.data_main
import com.kpstv.yts.models.response.Model
import com.kpstv.yts.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.await
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(
    application: Application,
    private val ytsPlaceholderApi: YTSPlaceholderApi,
    private val repository: MainRepository,
    private val favouriteRepository: FavouriteRepository,
    private val pauseRepository: PauseRepository,
    private val downloadRepository: DownloadRepository
) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"

    private val context = application.applicationContext

    val favouriteMovieIds by lazyDeferred {
        favouriteRepository.getAllMovieId()
    }

    val downloadMovieIds by lazyDeferred {
        downloadRepository.getAllDownloads()
    }

    val pauseMovieJob by lazyDeferred {
        pauseRepository.getAllPauseJob()
    }

    fun removeDownload(hash: String) =
        downloadRepository.deleteDownload(hash)

    fun updateDownload(hash: String, recentlyPlayed: Boolean, lastPosition: Int) =
        downloadRepository.updateDownload(hash, recentlyPlayed, lastPosition)

    fun isFavourite(listener: (Boolean) -> Unit, movieId: Int) {
        Coroutines.main {
            listener.invoke(AppUtils.isMovieFavourite(favouriteRepository, movieId))
        }
    }

    fun removeFavourite(movieId: Int) =
        favouriteRepository.deleteMovie(movieId)

    fun addToFavourite(model: Model.response_favourite) =
        favouriteRepository.saveMovie(model)

    fun getYTSQuery(moviesListener: MoviesListener, queryMap: Map<String, String>) {
        moviesListener.onStarted()

        Coroutines.main {
            try {
                val queryString = QueryConverter.fromMapToString(queryMap)

                if (isFetchNeeded(queryString)) {
                    Log.e(TAG, "==> Fetching New data")
                    fetchNewData(moviesListener, queryMap)
                } else {
                    Log.e(TAG, "==> Getting data from repository")

                    repository.getMoviesByQuery(
                        queryString
                    )?.let {
                        moviesListener.onComplete(it.movies, queryMap, it.isMore)
                    }
                }

            } catch (e: Exception) {
                moviesListener.onFailure(e)
            }
        }
    }

    fun getFeaturedMovies(moviesListener: MoviesListener) {
        moviesListener.onStarted()

        Coroutines.main {
            try {
                val queryString = "movies=featured&client=yts"
                val queryMap = QueryConverter.toMapfromString(queryString)
                if (isFetchNeeded(queryString)) {

                    Log.e(TAG, "==> Fetching new data")

                    fetchFeaturedData(moviesListener,queryString)

                } else {
                    Log.e(TAG, "==> Getting data from repository")

                    repository.getMoviesByQuery(
                        queryString
                    )?.let {
                        moviesListener.onComplete(it.movies ,queryMap, it.isMore)
                    }
                }

            }catch (e: Exception) {
                moviesListener.onFailure(e)
            }
        }
    }

    private suspend fun fetchFeaturedData(moviesListener: MoviesListener, queryString: String) {
        val list = ArrayList<MovieShort>()
        val doc = withContext(Dispatchers.IO) {
            Jsoup.connect(AppInterface.YTS_BASE_URL).get()
        }
        val elements = doc.getElementsByClass("browse-movie-link")
        for (i in 0..3) {
            val link = elements[i].attr("href").toString()
            val subDoc = withContext(Dispatchers.IO) {
                Jsoup.connect(link).get()
            }

            val movieId =
                subDoc.getElementById("movie-info").attr("data-movie-id").toString().toInt()
            var imdbCode = ""
            var rating = 0.0
            subDoc.getElementsByClass("rating-row").forEach {
                if (it.hasAttr("itemscope")) {
                    imdbCode = it.getElementsByClass("icon")[0]
                        .attr("href").toString().split("/")[4]
                    it.allElements.forEach {
                        if (it.hasAttr("itemprop") && it.attr("itemprop")
                                .toString() == "ratingValue"
                        ) {
                            rating = it.ownText().toDouble()
                        }
                    }
                }
            }

            var title = ""
            var year = 0
            var bannerUrl = ""
            var runtime = 0

            subDoc.getElementById("mobile-movie-info").allElements.forEach {
                if (it.hasAttr("itemprop"))
                    title = it.ownText()
                else
                    if (it.ownText().isNotBlank() && it.ownText().isDigitsOnly())
                        year = it.ownText().toInt()
            }

            subDoc.getElementById("movie-poster").allElements.forEach {
                if (it.hasAttr("itemprop"))
                    bannerUrl = it.attr("src").toString()
            }

            subDoc.getElementsByClass("icon-clock")[0]?.let {
                val runtimeString = it.parent().ownText().trim()
                if (runtimeString.contains("hr")) {
                    runtime = runtimeString.split("hr")[0].trim().toInt() * 60
                    if (runtimeString.contains("min"))
                        runtime += runtimeString.split(" ")[2].trim().toInt()
                    return@let
                }
                if (runtimeString.contains("min"))
                    runtime += runtimeString.split("min")[0].trim().toInt()
            }

            list.add(
                MovieShort(movieId, link, title, year, rating, runtime, imdbCode, bannerUrl)
            )
        }

        val mainModel = data_main(
            time = MainDateFormatter.format(Calendar.getInstance().time).toLong(),
            movies = list,
            query = queryString,
            isMore = false
        )

        moviesListener.onComplete(
            list,
            QueryConverter.toMapfromString(queryString),
            false
        )

        repository.saveMovies(mainModel)
    }

    private suspend fun fetchNewData(
        moviesListener: MoviesListener,
        queryMap: Map<String, String>
    ) {
        val response = ytsPlaceholderApi.listMovies(queryMap).await()
        if (response.data.movie_count > 0) {

            val list = response.data.movies

            var toIndex = CUSTOM_LAYOUT_YTS_SPAN
            if (list?.size!! < CUSTOM_LAYOUT_YTS_SPAN+1) toIndex = list.size

            val movieList = ArrayList<MovieShort>()
            ArrayList(response.data.movies.subList(0, toIndex)).forEach {
                movieList.add(
                    MovieShort(
                        it.id,
                        it.url,
                        it.title,
                        it.year,
                        it.rating,
                        it.runtime,
                        it.imdb_code,
                        it.medium_cover_image
                    )
                )
            }

            val isMoreAvailable = response.data.movie_count > CUSTOM_LAYOUT_YTS_SPAN

            val mainModel = data_main(
                time = MainDateFormatter.format(Calendar.getInstance().time).toLong(),
                movies = movieList,
                query = QueryConverter.fromMapToString(queryMap),
                isMore = isMoreAvailable
            )

            moviesListener.onComplete(
                movieList,
                queryMap,
                isMoreAvailable
            )

            repository.saveMovies(mainModel)

        } else moviesListener.onFailure(Exception("Empty movie list"))
    }

    private suspend fun isFetchNeeded(queryString: String): Boolean {
        try {
            val movieModel = repository.getMoviesByQuery(queryString)
            movieModel?.also {
                val currentCalender = Calendar.getInstance()
                currentCalender.add(Calendar.HOUR, -QUERY_SPAN_DIFFERENCE)
                val currentSpan = MainDateFormatter.format(
                    currentCalender.time
                ).toLong()
                return if (currentSpan > movieModel.time) {
                    repository.deleteMovies(movieModel)
                    true
                } else false
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }
}