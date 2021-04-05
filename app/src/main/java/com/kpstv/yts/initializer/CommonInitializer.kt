package com.kpstv.yts.initializer

import android.content.Context
import androidx.startup.Initializer
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.kpstv.after.After
import com.kpstv.common_moviesy.extensions.loadFont
import com.kpstv.yts.AppInterface
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.ui.activities.CrashOnActivity
import es.dmoral.toasty.Toasty

// Light weight configuration should be added here.

@Suppress("unused")
class CommonInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        context.loadFont(R.font.google_sans_regular)?.let { typeface ->
            /** Change Toasty font */
            Toasty.Config.getInstance()
                .setTextSize(14)
                .setToastTypeface(typeface)
                .apply()

            /** Set configs for After */
            After.Config
                .setTypeface(typeface)
                .setTextSize(15f)
        }

        /** Setting up notifications */
        Notifications.setup(context)

        /** Set some BuildConfigs */
        AppInterface.TMDB_API_KEY = BuildConfig.TMDB_API_KEY

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