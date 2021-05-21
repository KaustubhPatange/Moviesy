package com.kpstv.yts.ui.viewmodels

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface.Companion.CUSTOM_LAYOUT_YTS_SPAN
import com.kpstv.yts.AppInterface.Companion.FEATURED_QUERY
import com.kpstv.yts.AppInterface.Companion.MainDateFormatter
import com.kpstv.yts.AppInterface.Companion.QUERY_SPAN_DIFFERENCE
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.data.db.localized.MainDao
import com.kpstv.yts.data.db.repository.DownloadRepository
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.data.db.repository.PauseRepository
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.data.data_main
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.MoviesCallback
import com.kpstv.yts.extensions.errors.SSLHandshakeException
import com.kpstv.yts.extensions.utils.YTSParser
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.ui.viewmodels.state.UIState
import kotlinx.coroutines.launch
import retrofit2.await
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
    private val ytsApi: YTSApi,
    private val repository: MainDao,
    private val favouriteRepository: FavouriteRepository,
    private val pauseRepository: PauseRepository,
    private val downloadRepository: DownloadRepository,
    private val ytsParser: YTSParser
) : ViewModel() {
    private val TAG = "MainViewModel"

    val uiState = UIState(savedStateHandle)

    val favouriteMovieIds by lazy { favouriteRepository.getAllMovieId() }
    val downloadMovieIds by lazy { downloadRepository.getAllDownloads() }
    val pauseMovieJob by lazy { pauseRepository.getAllPauseJob() }

    fun removeDownload(hash: String) = downloadRepository.deleteDownload(hash)

    fun updateDownload(hash: String, recentlyPlayed: Boolean, lastPosition: Int) = viewModelScope.launch {
        downloadRepository.updateAllNormalDownloads()
        downloadRepository.updateDownload(hash, recentlyPlayed, lastPosition)
    }

    fun isMovieFavourite(movieId: Int, listener: (Boolean) -> Unit) {
        viewModelScope.launch {
            listener.invoke(favouriteRepository.isMovieFavourite(movieId))
        }
    }

    fun removeFavourite(movieId: Int) {
        viewModelScope.launch {
            favouriteRepository.deleteMovie(movieId)
        }
    }

    fun addToFavourite(model: Model.response_favourite) {
        viewModelScope.launch {
            favouriteRepository.saveMovie(model)
        }
    }

    fun removeYtsQuery(queryMap: Map<String, String>) {
        viewModelScope.launch {
            val queryString = QueryConverter.fromMapToString(queryMap)
            repository.removeMoviesByQuery(queryString)
        }
    }

    fun getYTSQuery(movieCallback: MoviesCallback, queryMap: Map<String, String>) {
        movieCallback.onStarted?.invoke()

        viewModelScope.launch {
            try {
                val queryString = QueryConverter.fromMapToString(queryMap)

                if (isFetchNeeded(queryString)) {
                    Log.e(TAG, "=> Fetching New data, $queryString")
                    fetchNewData(movieCallback, queryMap)
                } else {
                    repository.getMoviesByQuery(
                        queryString
                    )?.let {
                        movieCallback.onComplete.invoke(
                            it.movies,
                            queryMap,
                            it.isMore
                        )
                    }
                }
            } catch (e: Exception) {
                movieCallback.onFailure?.invoke(e)
            }
        }
    }

    fun getFeaturedMovies(moviesCallback: MoviesCallback) {
        moviesCallback.onStarted?.invoke()

        viewModelScope.launch {
            val queryMap = QueryConverter.toMapfromString(FEATURED_QUERY)
            try {
                if (isFetchNeeded(FEATURED_QUERY)) {
                    Log.e(TAG, "=> Featured: Fetching new data")
                    fetchFeaturedData(moviesCallback)
                } else {
                    repository.getMoviesByQuery(FEATURED_QUERY)?.let {
                        moviesCallback.onComplete(it.movies, queryMap, it.isMore)
                    }
                }
            } catch (e: Exception) {
                if (e is SSLHandshakeException || e is javax.net.ssl.SSLHandshakeException) {
                    repository.getMoviesByQuery(FEATURED_QUERY)?.let {
                        moviesCallback.onComplete(it.movies, queryMap, it.isMore)
                    }
                } else {
                    moviesCallback.onFailure?.invoke(e)
                }
            }
        }
    }

    private suspend fun fetchFeaturedData(movieCallback: MoviesCallback) {
        val list = ytsParser.fetchFeaturedMovies()
        if (list.isNotEmpty()) {
            val mainModel = data_main(
                time = MainDateFormatter.format(Calendar.getInstance().time).toLong(),
                movies = list,
                query = FEATURED_QUERY,
                isMore = false
            )

            repository.saveMovies(mainModel)

            movieCallback.onComplete.invoke(
                list,
                QueryConverter.toMapfromString(FEATURED_QUERY),
                false
            )
        } else movieCallback.onFailure?.invoke(Exception("Empty movie list"))
    }

    private suspend fun fetchNewData(
        movieCallback: MoviesCallback,
        queryMap: Map<String, String>
    ) {
        val response = ytsApi.listMovies(queryMap).await()
        if (response.data.movie_count > 0) {

            val list = response.data.movies

            var toIndex = CUSTOM_LAYOUT_YTS_SPAN
            if (list?.size!! < CUSTOM_LAYOUT_YTS_SPAN + 1) toIndex = list.size

            val movieList = ArrayList<MovieShort>()
            ArrayList(response.data.movies.subList(0, toIndex)).forEach {
                movieList.add(MovieShort.from(it))
            }

            val isMoreAvailable = response.data.movie_count > CUSTOM_LAYOUT_YTS_SPAN

            val mainModel =
                data_main.from(movieList, QueryConverter.fromMapToString(queryMap), isMoreAvailable)

            repository.saveMovies(mainModel)

            movieCallback.onComplete.invoke(
                movieList,
                queryMap,
                isMoreAvailable
            )

        } else movieCallback.onFailure?.invoke(Exception("Empty movie list"))
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
                    repository.deleteMovie(movieModel)
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