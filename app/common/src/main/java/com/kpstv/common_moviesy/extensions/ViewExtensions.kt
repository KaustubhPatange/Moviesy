package com.kpstv.common_moviesy.extensions

import android.graphics.Rect
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding

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

fun View.applyBottomInsets(to: View = this, pad: Boolean = false) {
    setOnApplyWindowInsetsListener { v, insets ->
        if (pad) {
            v.updatePadding(bottom = insets.systemWindowInsetBottom)
        } else {
            to.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = insets.systemWindowInsetBottom)
            }
        }
        insets
    }
}

fun View.applyTopInsets(to: View = this, pad: Boolean = false) {
    setOnApplyWindowInsetsListener { v, insets ->
        if (pad) {
            v.updatePadding(top = insets.systemWindowInsetTop)
        } else {
            to.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(top = insets.systemWindowInsetTop)
            }
        }
        insets
    }
}

fun View.globalVisibleRect(): Rect {
    val rect = Rect()
    getGlobalVisibleRect(rect)
    return rect
}