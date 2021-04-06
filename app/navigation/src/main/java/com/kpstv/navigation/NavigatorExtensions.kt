package com.kpstv.navigation

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.Fragment

/**
 * Determines if there is no [Fragment] in [FragmentManager] backStack & the
 * current [Fragment] is the last one in the backStack.
 */
fun Navigator.canFinish() : Boolean {
    if (canGoBack()) {
        goBack()
        return false
    }
    return true
}