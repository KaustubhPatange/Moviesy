package com.kpstv.yts.di

import android.app.Application
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.facebook.stetho.Stetho
import com.google.android.gms.ads.MobileAds
import com.kpstv.after.After
import com.kpstv.yts.AppInterface
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.services.AppWorker
import dagger.hilt.android.HiltAndroidApp
import es.dmoral.toasty.Toasty
import javax.inject.Inject

@Suppress("unused")
@HiltAndroidApp
class ApplicationClass : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        /** Change Toasty font */
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.google_sans_regular)
        Toasty.Config.getInstance()
            .setTextSize(14)
            .setToastTypeface(typeface!!)
            .apply()

        /** Initialize mobile ads */
        MobileAds.initialize(this) { }

        /** Setting up notifications */
        Notifications.setup(applicationContext)

        /** Scheduling work manager */
        AppWorker.schedule(applicationContext)

        /** Set some BuildConfigs */
        AppInterface.TMDB_API_KEY = BuildConfig.TMDB_API_KEY

        /** Set configs for After */
        After.Config
            .setTypeface(typeface)
            .setTextSize(14)

        /** Initializing Stetho */
        if (BuildConfig.DEBUG)
            Stetho.initializeWithDefaults(this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}