package com.kpstv.yts.ui.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.CUSTOM_LAYOUT_YTS_SPAN
import com.kpstv.yts.AppInterface.Companion.MainDateFormatter
import com.kpstv.yts.AppInterface.Companion.QUERY_SPAN_DIFFERENCE
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.data.db.repository.DownloadRepository
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.data.db.repository.MainRepository
import com.kpstv.yts.data.db.repository.PauseRepository
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface.Companion.FEATURED_QUERY
import com.kpstv.yts.extensions.lazyDeferred
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.interfaces.listener.MoviesListener
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.data.data_main
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.YTSFeaturedUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.await
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel @ViewModelInject constructor(
    application: Application,
    private val ytsApi: YTSApi,
    private val repository: MainRepository,
    private val favouriteRepository: FavouriteRepository,
    private val pauseRepository: PauseRepository,
    private val downloadRepository: DownloadRepository,
    private val ytsFeaturedUtils: YTSFeaturedUtils
) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"

    /** We will save all fragment views into this ViewModel
     *  This a workaround to handle fragment UI state easily but
     *  memory consuming.
     */
    var homeView: View? = null
    var watchView: View? = null
    var libraryView: View? = null

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

    fun updateDownload(hash: String, recentlyPlayed: Boolean, lastPosition: Int) = Coroutines.io {
        downloadRepository.updateAllNormalDownloads()
        downloadRepository.updateDownload(hash, recentlyPlayed, lastPosition)
    }

    fun isFavourite(listener: (Boolean) -> Unit, movieId: Int) {
        Coroutines.main {
            listener.invoke(AppUtils.isMovieFavourite(favouriteRepository, movieId))
        }
    }

    fun removeFavourite(movieId: Int) =
        favouriteRepository.deleteMovie(movieId)

    fun addToFavourite(model: Model.response_favourite) =
        favouriteRepository.saveMovie(model)

    fun removeYtsQuery(queryMap: Map<String, String>) {
        val queryString = QueryConverter.fromMapToString(queryMap)
        repository.removeMoviesByQuery(queryString)
    }

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
                val queryMap = QueryConverter.toMapfromString(FEATURED_QUERY)
                if (isFetchNeeded(FEATURED_QUERY)) {

                    Log.e(TAG, "==> Fetching new data")

                    fetchFeaturedData(moviesListener,FEATURED_QUERY)

                } else {
                    Log.e(TAG, "==> Getting data from repository")

                    repository.getMoviesByQuery(
                        FEATURED_QUERY
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
        val list = ytsFeaturedUtils.fetch()

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
        val response = ytsApi.listMovies(queryMap).await()
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