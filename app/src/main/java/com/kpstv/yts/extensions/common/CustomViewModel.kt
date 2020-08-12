package com.kpstv.yts.extensions.common

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import com.kpstv.yts.data.models.MovieShort

class CustomViewModel: ViewModel() {
    var customLayoutMap: HashMap<String, Parcelable?>? = null
    var customModelMap: HashMap<String, ArrayList<MovieShort>>? = null
}