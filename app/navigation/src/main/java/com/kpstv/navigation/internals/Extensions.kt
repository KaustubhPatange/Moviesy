package com.kpstv.navigation

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

internal fun View.doOnLaidOut(block: () -> Unit) {
    if (isLaidOut) {
        block.invoke()
    } else {
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener{
            override fun onPreDraw(): Boolean {
                if (isLaidOut) {
                    block.invoke()
                    viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })
    }
}