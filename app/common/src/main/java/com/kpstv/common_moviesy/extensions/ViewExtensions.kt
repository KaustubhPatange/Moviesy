package com.kpstv.common_moviesy.extensions

import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup

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