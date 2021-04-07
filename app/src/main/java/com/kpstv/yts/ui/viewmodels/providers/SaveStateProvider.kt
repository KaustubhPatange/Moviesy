package com.kpstv.yts.ui.viewmodels.providers

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle


class HomeState(private val savedStateHandle: SavedStateHandle) {
    companion object {
        private const val TAB_POSITION = "home_tab_position"
        private const val APP_BAR_EXPANDED = "home_is_app_bar_expanded"
    }
    var tabPosition: Int?
        get() = savedStateHandle.get(TAB_POSITION)
        set(value) = savedStateHandle.set(TAB_POSITION, value)
    var isAppBarExpanded: Boolean?
        get() = savedStateHandle.get(APP_BAR_EXPANDED)
        set(value) = savedStateHandle.set(APP_BAR_EXPANDED, value)
}

class WatchState(private val savedStateHandle: SavedStateHandle) {
    companion object {
        private const val RECYCLERVIEW_STATE = "watch_recyclerview_state"
        private const val APP_BAR_EXPANDED = "watch_is_app_bar_expanded"
    }
    var recyclerViewState: Parcelable?
        get() = savedStateHandle.get(RECYCLERVIEW_STATE)
        set(value) = savedStateHandle.set(RECYCLERVIEW_STATE, value)

    var isAppBarExpanded: Boolean?
        get() = savedStateHandle.get(APP_BAR_EXPANDED)
        set(value) = savedStateHandle.set(APP_BAR_EXPANDED, value)
}

class LibraryState(private val savedStateHandle: SavedStateHandle) {
    companion object {
        private const val RECYCLERVIEW_STATE = "library_recyclerview_state"
    }
    var recyclerViewState: Parcelable?
        get() = savedStateHandle.get(RECYCLERVIEW_STATE)
        set(value) = savedStateHandle.set(RECYCLERVIEW_STATE, value)
}

class ChartState(private val savedStateHandle: SavedStateHandle) {
    companion object {
        private const val NESTED_SCROLL_STATE = "chart_recyclerview_state"
    }
    var nestedScrollState: Parcelable?
        get() = savedStateHandle.get(NESTED_SCROLL_STATE)
        set(value) = savedStateHandle.set(NESTED_SCROLL_STATE, value)
}

class GenreState(private val savedStateHandle: SavedStateHandle) {
    companion object {
        private const val RECYCLERVIEW_STATE = "genre_recyclerview_state"
    }
    var recyclerViewState: Parcelable?
        get() = savedStateHandle.get(RECYCLERVIEW_STATE)
        set(value) = savedStateHandle.set(RECYCLERVIEW_STATE, value)
}

/** TODO: You can remove the below code. */
class OldWatchState {
    var recyclerViewState: Parcelable? = null
    var isAppBarExpanded: Boolean? = null
}

class OldLibraryState {
    var recyclerViewState: Parcelable? = null
}

class OldHomeState {
    var tabPosition: Int? = null
    var isAppBarExpanded: Boolean? = null
}

class OldChartState {
    var nestedScrollState: Parcelable? = null
}

class OldGenreState {
    var recyclerViewState: Parcelable? = null
}
