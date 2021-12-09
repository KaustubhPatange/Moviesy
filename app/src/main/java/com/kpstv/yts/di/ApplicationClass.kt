package com.kpstv.yts.di

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.ui.helpers.ThemeHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@Suppress("unused")
@HiltAndroidApp
class ApplicationClass : Application(), Configuration.Provider {

    private val appPreference by defaultPreference()

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        ThemeHelper.updateValues(appPreference)
    }
}