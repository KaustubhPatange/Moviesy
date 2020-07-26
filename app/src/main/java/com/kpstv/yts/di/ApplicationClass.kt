package com.kpstv.yts.di

import android.app.Application
import androidx.core.content.res.ResourcesCompat
import com.kpstv.yts.R
import dagger.hilt.android.HiltAndroidApp
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON

@Suppress("unused")
@HiltAndroidApp
class ApplicationClass: Application() {
    override fun onCreate() {
        super.onCreate()
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.google_sans_regular)
        Toasty.Config.getInstance()
            .setTextSize(14)
            .setToastTypeface(typeface!!)
            .apply()
    }
}