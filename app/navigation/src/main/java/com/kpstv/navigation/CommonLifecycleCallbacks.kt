package com.kpstv.navigation

import android.os.Bundle
import androidx.lifecycle.LifecycleCoroutineScope

internal interface CommonLifecycleCallbacks {
    fun onCreate(lifecycleScope: LifecycleCoroutineScope, savedInstanceState: Bundle?)
    fun onSaveInstanceState(outState: Bundle)
}