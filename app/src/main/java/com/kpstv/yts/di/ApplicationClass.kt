package com.kpstv.yts.di

import android.app.Application
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.MobileAds
import com.kpstv.yts.R
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.services.AppWorker
import com.kpstv.yts.services.DownloadService
import dagger.hilt.android.HiltAndroidApp
import es.dmoral.toasty.Toasty

@Suppress("unused")
@HiltAndroidApp
class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        /** Change Toasty font */
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.google_sans_regular)
        Toasty.Config.getInstance()
            .setTextSize(14)
            .setToastTypeface(typeface!!)
            .apply()

        /** Initialize mobile ads */
        MobileAds.initialize(this) {}

        /** Setting up notifications */
        Notifications.setup(applicationContext)
        //PRDownloader.initialize(applicationContext)
    }
}