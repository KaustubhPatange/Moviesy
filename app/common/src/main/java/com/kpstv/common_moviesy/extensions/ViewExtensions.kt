package com.kpstv.common_moviesy.extensions

import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.enableDelayedTransition() = TransitionManager.beginDelayedTransition(this as ViewGroup)

fun View.applyBottomInsets() {
    setOnApplyWindowInsetsListener { v, insets ->
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(bottom = insets.systemWindowInsetBottom)
        }
        insets
    }
}