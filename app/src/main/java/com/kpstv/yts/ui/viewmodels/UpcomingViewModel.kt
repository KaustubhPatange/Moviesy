package com.kpstv.yts.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.UPCOMING_QUERY
import com.kpstv.yts.data.db.localized.MainDao
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.data.data_main
import com.kpstv.yts.extensions.utils.YTSParser
import java.util.*
import kotlin.collections.ArrayList

class UpcomingViewModel @ViewModelInject constructor(
    private val repository: MainDao,
    private val ytsParser: YTSParser
) : ViewModel() {

    fun fetchUpcomingMovies(forceRefresh: Boolean = false): LiveData<UpcomingUiState> = liveData {
        emit(UpcomingUiState.Loading)
        try {
            if (forceRefresh || isFetchNeeded(UPCOMING_QUERY)) {
                fetchMovies()
            } else {
                repository.getMoviesByQuery(UPCOMING_QUERY)?.let { data ->
                    emit(UpcomingUiState.Success(data.movies))
                }
            }
        } catch (e: Exception) {
            emit(UpcomingUiState.Error)
        }
    }

    private suspend fun LiveDataScope<UpcomingUiState>.fetchMovies() {
        val movies = ytsParser.fetchUpcomingMovies()
        if (movies.isNotEmpty()) {
            val mainModel = data_main(
                time = AppInterface.MainDateFormatter.format(Calendar.getInstance().time).toLong(),
                movies = movies,
                query = UPCOMING_QUERY,
                isMore = false
            )

            repository.saveMovies(mainModel)

            emit(UpcomingUiState.Success(movies))
        } else {
            emit(UpcomingUiState.Error)
        }
    }

    private suspend fun isFetchNeeded(queryString: String): Boolean {
        return ytsParser.isMovieFetchNeeded(queryString, repository)
    }
}

sealed class UpcomingUiState {
    object Loading : UpcomingUiState()
    data class Success(val data: ArrayList<MovieShort>) : UpcomingUiState()
    object Error : UpcomingUiState()
}