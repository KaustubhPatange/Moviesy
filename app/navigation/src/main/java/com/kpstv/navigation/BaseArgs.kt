package com.kpstv.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseArgs : Parcelable {
    open fun getTag(): String? {
        return null
    }
}