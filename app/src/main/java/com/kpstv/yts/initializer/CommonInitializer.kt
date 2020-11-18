package com.kpstv.yts.initializer

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.startup.Initializer
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.android.gms.ads.MobileAds
import com.kpstv.after.After
import com.kpstv.yts.AppInterface
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.services.AppWorker
import com.kpstv.yts.services.LatestMovieWorker
import com.kpstv.yts.ui.activities.CrashOnActivity
import es.dmoral.toasty.Toasty

class CommonInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        /** Change Toasty font */
        val typeface = ResourcesCompat.getFont(context, R.font.google_sans_regular)
        Toasty.Config.getInstance()
            .setTextSize(14)
            .setToastTypeface(typeface!!)
            .apply()

        /** Setting up notifications */
        Notifications.setup(context)

        /** Set some BuildConfigs */
        AppInterface.TMDB_API_KEY = BuildConfig.TMDB_API_KEY

        /** Set configs for After */
        After.Config
            .setTypeface(typeface)
            .setTextSize(14)

        /** Set CAOC config */
        setCaocConfig()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    private fun setCaocConfig() {
        CaocConfig.Builder.create()
            .errorActivity(CrashOnActivity::class.java)
            .apply()
    }
}