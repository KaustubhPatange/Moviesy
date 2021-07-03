package com.kpstv.yts.ui.helpers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.kpstv.yts.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardAdHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private lateinit var rewardedAd: RewardedAd

    fun init() {
        loadAd()
    }

    fun showAd(activity: Activity, invoke: () -> Unit) {
        if (!rewardedAd.isLoaded) {
            createRewardAd()
            rewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    showAd(activity, invoke)
                }
            })
            return
        }
        rewardedAd.show(activity, object : RewardedAdCallback(){
            override fun onRewardedAdFailedToShow(p0: AdError?) {
                loadAd()
            }

            override fun onRewardedAdClosed() {
               loadAd()
            }
            override fun onUserEarnedReward(p0: RewardItem) {
                loadAd()
                invoke()
            }
        })
    }

    private fun createRewardAd() {
        rewardedAd = RewardedAd(
            context,
            if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917"
            else BuildConfig.REWARD_AD_ID
        )
    }

    private fun loadAd() {
        createRewardAd()
        val adRequest = AdRequest.Builder().build()
        rewardedAd.loadAd(adRequest, loadRequest)
    }

    private val loadRequest = object : RewardedAdLoadCallback() {
        override fun onRewardedAdFailedToLoad(error: LoadAdError?) {
            Log.e("RewardedAd", "Failed to load: $error")
        }
    }
}