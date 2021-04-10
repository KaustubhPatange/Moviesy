package com.kpstv.common_moviesy.extensions

import android.content.res.Resources

fun Int.dp(): Float = Resources.getSystem().displayMetrics.density * this