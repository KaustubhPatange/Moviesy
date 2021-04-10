package com.kpstv.common_moviesy.extensions

import android.content.Context
import android.graphics.Rect
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.marginBottom
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

fun View.applyBottomInsets(to: View = this, merge: Boolean = false, pad: Boolean = false, extra: Int = 0) {
    setOnApplyWindowInsetsListener { v, insets ->
        if (pad) {
            v.updatePadding(bottom = insets.systemWindowInsetBottom + extra + if (merge) v.paddingBottom else 0)
        } else {
            to.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = insets.systemWindowInsetBottom + extra + if (merge) v.marginBottom else 0)
            }
        }
        insets
    }
}

fun View.applyTopInsets(to: View = this, merge: Boolean = false, pad: Boolean = false, extra: Int = 0) {
    setOnApplyWindowInsetsListener { v, insets ->
        if (pad) {
            v.updatePadding(top = insets.systemWindowInsetTop + extra + if (merge) v.paddingBottom else 0)
        } else {
            to.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(top = insets.systemWindowInsetTop + extra + if (merge) v.marginBottom else 0)
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

fun View.scaleInOut() {
    animate().scaleX(1.2f).scaleY(1.2f)
        .withEndAction {
            scaleX = 1f
            scaleY = 1f
        }
        .start()
}

fun View.showKeyboard() {
    val inputMethodManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInputFromWindow(applicationWindowToken, InputMethodManager.SHOW_IMPLICIT, 0)
    requestFocus()
    if (this is EditText) {
        setSelection(text.length)
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}