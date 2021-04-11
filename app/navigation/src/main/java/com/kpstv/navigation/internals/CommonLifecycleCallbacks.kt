package com.kpstv.navigation.internals

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope

internal interface CommonLifecycleCallbacks {
    fun onCreate(savedInstanceState: Bundle?)
    fun onSaveInstanceState(outState: Bundle)
}