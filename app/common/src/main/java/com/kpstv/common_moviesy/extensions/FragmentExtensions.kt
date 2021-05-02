package com.kpstv.common_moviesy.extensions

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

fun Fragment.colorFrom(@ColorRes color: Int) = requireContext().colorFrom(color)

fun Fragment.drawableFrom(@DrawableRes value: Int) = requireContext().drawableFrom(value)

fun Fragment.rootParentFragment(): Fragment {
    val fragment = parentFragment
    return fragment?.rootParentFragment() ?: this
}