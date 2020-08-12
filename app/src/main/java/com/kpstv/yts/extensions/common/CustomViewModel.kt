package com.kpstv.yts.extensions.common

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import com.kpstv.yts.data.models.MovieShort

class CustomViewModel: ViewModel() {
    var customLayoutRecyclerView: HashMap<String, Parcelable?> = HashMap()
    var customModelMap: HashMap<String, ArrayList<MovieShort>> = HashMap()
    var customLayoutConfig: HashMap<String, CustomMovieLayout.CustomLayoutConfig> = HashMap()
}