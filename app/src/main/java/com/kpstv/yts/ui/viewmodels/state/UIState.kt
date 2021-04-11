package com.kpstv.yts.ui.viewmodels.state

import androidx.lifecycle.SavedStateHandle
import com.kpstv.yts.ui.viewmodels.providers.*
import javax.inject.Inject
import javax.inject.Singleton

class UIState(savedStateHandle: SavedStateHandle) {
    val mainFragmentState = MainState(savedStateHandle)
    val watchFragmentState = WatchState(savedStateHandle)
    val libraryFragmentState = LibraryState(savedStateHandle)
    val homeFragmentState = HomeState(savedStateHandle)
    val chartFragmentState = ChartState(savedStateHandle)
    val genreFragmentState = GenreState(savedStateHandle)
}