package com.kpstv.navigation

import androidx.core.view.children
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

/**
 * A hot fix to overcome Z-index issue of the views in fragment container.
 */
fun Navigator.autoChildElevation() {
    getFragmentManager().addOnBackStackChangedListener {
        getContainerView().children.forEachIndexed { index, view ->
            view.translationZ = (index + 1).toFloat()
        }
    }
}

/**
 * This will clear the [BaseArgs].
 */
fun ValueFragment.clearArgs() {
    arguments?.remove(ValueFragment.ARGUMENTS)
}