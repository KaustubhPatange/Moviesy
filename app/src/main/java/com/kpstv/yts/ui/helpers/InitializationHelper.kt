package com.kpstv.yts.ui.helpers

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.kpstv.yts.services.AppWorker
import com.kpstv.yts.services.LatestMovieWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Heavy weight dependency should be configured here as it increases app startup time
// Typically initializing MobileAds & scheduling work-manager take immense amount of time
@Singleton
class InitializationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interstitialAdHelper: InterstitialAdHelper,
    private val rewardAdHelper: RewardAdHelper
) {

    private var isInitialized: Boolean = false

    fun initializeDependencies() {
        if (isInitialized) return

        isInitialized = true

        MobileAds.initialize(context) {
            interstitialAdHelper.init()
            rewardAdHelper.init()
        }

        AppWorker.schedule(context)
        LatestMovieWorker.schedule(context)
    }
}