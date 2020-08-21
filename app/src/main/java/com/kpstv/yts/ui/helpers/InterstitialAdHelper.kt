package com.kpstv.yts.ui.helpers

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.kpstv.yts.AppInterface
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.extensions.SimpleCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = javaClass.simpleName

    private var mInterstitialAd: InterstitialAd = InterstitialAd(context)
    private var onAdClosed: SimpleCallback? = null

    fun init() {
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                mInterstitialAd.loadAd(AdRequest.Builder().build())

                invokeCallback()
            }

            override fun onAdFailedToLoad(p0: LoadAdError?) {
                super.onAdFailedToLoad(p0)
                mInterstitialAd.loadAd(AdRequest.Builder().build())

                invokeCallback()
            }
        }

        if (BuildConfig.DEBUG)
            mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        else
            mInterstitialAd.adUnitId = BuildConfig.INTERSTITIAL_ID

        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun invokeCallback() {
        try {
            onAdClosed?.invoke()
        } catch (e: Exception) {
            Log.w(TAG, "Something unexpected happened", e)
        }
    }

    fun showAd(onAdClosed: SimpleCallback? = null) {
        this.onAdClosed = onAdClosed

        if (AppInterface.IS_PREMIUM_UNLOCKED) {
            onAdClosed?.invoke()
            return
        }

        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.e(TAG, "The interstitial wasn't loaded yet.")
            onAdClosed?.invoke()
        }
    }
}