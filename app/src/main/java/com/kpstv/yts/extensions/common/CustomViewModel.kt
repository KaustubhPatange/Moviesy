package com.kpstv.yts.extensions.common

import android.os.Parcelable
import androidx.lifecycle.ViewModel

class CustomViewModel: ViewModel() {
    var customLayoutMap: HashMap<String, Parcelable?>? = null
}