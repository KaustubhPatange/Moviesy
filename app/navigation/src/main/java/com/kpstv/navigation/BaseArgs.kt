package com.kpstv.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Extend this class to have generic typed arguments for [ValueFragment].
 */
@Parcelize
open class BaseArgs : Parcelable {
    open fun getTag(): String? {
        return null
    }
}