package com.kpstv.yts.di

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.kpstv.yts.services.AppWorker
import com.kpstv.yts.services.LatestMovieWorker
import com.kpstv.yts.ui.helpers.InterstitialAdHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@Suppress("unused")
@HiltAndroidApp
class ApplicationClass : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var interstitialAdHelper: InterstitialAdHelper

    override fun onCreate() {
        super.onCreate()
        /** Initialize mobile ads */
        MobileAds.initialize(this) {
            interstitialAdHelper.init()
        }

        /** Scheduling work manager */
        AppWorker.schedule(applicationContext)
        LatestMovieWorker.schedule(applicationContext)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}