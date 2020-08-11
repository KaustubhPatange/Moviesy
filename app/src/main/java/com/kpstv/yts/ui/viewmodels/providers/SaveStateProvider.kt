package com.kpstv.yts.ui.viewmodels.providers

import android.os.Parcelable

open class SaveStateProvider {
    var recyclerViewState: Parcelable? = null
}

class WatchState: SaveStateProvider() {
    var isAppBarExpanded: Boolean? = null
}

class LibraryState: SaveStateProvider() {

}