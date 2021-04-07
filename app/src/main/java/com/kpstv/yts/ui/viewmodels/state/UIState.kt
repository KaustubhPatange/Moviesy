package com.kpstv.yts.ui.viewmodels.state

import androidx.lifecycle.SavedStateHandle
import com.kpstv.yts.ui.viewmodels.providers.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIState2 @Inject constructor() {
    val watchFragmentState = OldWatchState()
    val libraryFragmentState = OldLibraryState()
    val homeFragmentState = OldHomeState()
    val chartFragmentState = OldChartState()
    val genreFragmentState = OldGenreState()
}

class UIState(savedStateHandle: SavedStateHandle) {
    val watchFragmentState = WatchState(savedStateHandle)
    val libraryFragmentState = LibraryState(savedStateHandle)
    val homeFragmentState = HomeState(savedStateHandle)
    val chartFragmentState = ChartState(savedStateHandle)
    val genreFragmentState = GenreState(savedStateHandle)
}