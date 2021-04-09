package com.kpstv.navigation.internals

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment

internal fun Fragment.getSaveInstanceState() : Bundle? {
    val field = Fragment::class.java.getDeclaredField("mSavedFragmentState").apply {
        isAccessible = true
    }
    return field.get(this) as? Bundle
}

internal fun View.doOnLaidOut(block: (View) -> Unit) {
    if (isLaidOut) {
        block.invoke(this)
    } else {
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener{
            override fun onPreDraw(): Boolean {
                if (isLaidOut) {
                    block.invoke(this@doOnLaidOut)
                    viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })
    }
}

internal fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.show() {
    visibility = View.VISIBLE
}