package com.kpstv.yts.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.kpstv.yts.extensions.lazyDeferred
import com.kpstv.yts.extensions.utils.YTSParser
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.ui.viewmodels.providers.*
import kotlinx.coroutines.launch
import retrofit2.await
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel @ViewModelInject constructor(
    application: Application,
    private val ytsApi: YTSApi,
    private val repository: MainDao,
    private val favouriteRepository: FavouriteRepository,
    private val pauseRepository: PauseRepository,
    private val downloadRepository: DownloadRepository,
    private val ytsParser: YTSParser
) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"

    val watchFragmentState = WatchState()
    val libraryFragmentState = LibraryState()
    val homeFragmentState = HomeState()
    val chartFragmentState = ChartState()
    val genreFragmentState = GenreState()

    private val _favouriteMovieIds = MutableLiveData<List<Model.response_favourite>>()
    val favouriteMovieIds: LiveData<List<Model.response_favourite>>
        get() = _favouriteMovieIds

    private val _downloadMovieIds = MutableLiveData<List<Model.response_download>>()
    val downloadMovieIds: LiveData<List<Model.response_download>>
        get() = _downloadMovieIds

    val pauseMovieJob by lazyDeferred {
        pauseRepository.getAllPauseJob()
    }

    fun removeDownload(hash: String) = downloadRepository.deleteDownload(hash)

    fun updateDownload(hash: String, recentlyPlayed: Boolean, lastPosition: Int) = Coroutines.io {
        downloadRepository.updateAllNormalDownloads()
        downloadRepository.updateDownload(hash, recentlyPlayed, lastPosition)
    }

    fun isMovieFavourite(listener: (Boolean) -> Unit, movieId: Int) {
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

        Coroutines.io {
            try {
                val queryString = QueryConverter.fromMapToString(queryMap)

                if (isFetchNeeded(queryString)) {
                    Log.e(TAG, "=> Fetching New data, $queryString")
                    fetchNewData(movieCallback, queryMap)
                } else {
                    Log.e(TAG, "=> Getting data from repository, $queryString")
                    repository.getMoviesByQuery(
                        queryString
                    )?.let {
                        Coroutines.main {
                            movieCallback.onComplete.invoke(
                                it.movies,
                                queryMap,
                                it.isMore
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Coroutines.main { movieCallback.onFailure?.invoke(e) }
            }
        }
    }

    fun getFeaturedMovies(moviesCallback: MoviesCallback) {
        moviesCallback.onStarted?.invoke()

        Coroutines.main {
            try {
                val queryMap = QueryConverter.toMapfromString(FEATURED_QUERY)
                if (isFetchNeeded(FEATURED_QUERY)) {
                    Log.e(TAG, "=> Featured: Fetching new data")
                    fetchFeaturedData(moviesCallback)
                } else {
                    Log.e(TAG, "=> Featured: Getting data from repository")
                    repository.getMoviesByQuery(
                        FEATURED_QUERY
                    )?.let {
                        moviesCallback.onComplete(
                            it.movies,
                            queryMap,
                            it.isMore
                        )
                    }
                }
            } catch (e: Exception) {
                moviesCallback.onFailure?.invoke(e)
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

            Coroutines.main {
                movieCallback.onComplete.invoke(
                    list,
                    QueryConverter.toMapfromString(FEATURED_QUERY),
                    false
                )
            }
        } else Coroutines.main { movieCallback.onFailure?.invoke(Exception("Empty movie list")) }
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

            Coroutines.main {
                movieCallback.onComplete.invoke(
                    movieList,
                    queryMap,
                    isMoreAvailable
                )
            }

        } else Coroutines.main { movieCallback.onFailure?.invoke(Exception("Empty movie list")) }
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

    init {
        favouriteRepository.getAllMovieId().observeForever {
            _favouriteMovieIds.value = it
        }
        downloadRepository.getAllDownloads().observeForever {
            _downloadMovieIds.value = it
        }
    }
}