package com.kpstv.yts.ui.viewmodels.state

import com.kpstv.yts.ui.viewmodels.providers.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIState @Inject constructor() {
    val watchFragmentState = WatchState()
    val libraryFragmentState = LibraryState()
    val homeFragmentState = HomeState()
    val chartFragmentState = ChartState()
    val genreFragmentState = GenreState()
}