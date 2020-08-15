package com.kpstv.yts.di

import android.app.Application
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.kpstv.yts.R
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.services.AppWorker
import com.kpstv.yts.services.DownloadService
import dagger.hilt.android.HiltAndroidApp
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}