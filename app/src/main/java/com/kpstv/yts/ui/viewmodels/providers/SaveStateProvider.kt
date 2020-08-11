package com.kpstv.yts.ui.viewmodels.providers

import android.os.Parcelable

class WatchState {
    var recyclerViewState: Parcelable? = null
    var isAppBarExpanded: Boolean? = null
}

class LibraryState {
    var recyclerViewState: Parcelable? = null
}

class HomeState {
    var tabPosition: Int? = null
    var isAppBarExpanded: Boolean? = null
}

class ChartState {
    var nestedScrollState: Parcelable? = null
}

class GenreState {
    var recyclerViewState: Parcelable? = null
}