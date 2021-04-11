package com.kpstv.yts.ui.helpers

import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.work.WorkManager
import com.kpstv.yts.R
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import com.kpstv.yts.ui.dialogs.WindowDialog
import java.util.*

object BatteryOptimizationHelper {
    /**
     * Moviesy uses [WorkManager] to schedules some periodic task when the app
     * is not active, but due to lot's of battery optimization restrictions it
     * does not guarantee that task will schedule at appropriate interval.
     *
     * There is also similar problem with interrupted torrent downloads &
     * unexpected chromecast session stopping.
     *
     * One of the main problem is many device OEMs have this implementation where
     * they force-stops app whenever user exits an app or clear it from recents.
     *
     * This stops all the background jobs and OS will queue them as pending jobs,
     * such jobs will be only run again when user opens the app again.
     *
     * Disabling battery optimization will at least ensure such force-stops does not
     * happen.
     *
     * There are other limitations like process death, app standby, low battery, &
     * plethora of other variables due to which a task will not dispatch.
     *
     * @see <a href="https://developer.android.com/training/monitoring-device-state/doze-standby.html">Optimize for Doze mode</a>
     * @see <a href="https://www.reddit.com/r/androiddev/comments/i4wkxq/best_way_for_dispatching_periodic_background_tasks/">Ideal way of dispatching periodic background task?</a>
     */
    fun askNoBatteryOptimization(context: Context) = with(context) {
        @Suppress("UNREACHABLE_CODE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val enabled = (getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
            if (!enabled && isSupportedVendor()) {
                WindowDialog.Builder(this)
                    .setTitle(R.string.doze_title)
                    .setSubtitle(R.string.doze_text)
                    .setLottieRes(R.raw.battery_opt)
                    .setCancellable(false)
                    .setNegativeButton(android.R.string.cancel)
                    .setPositiveButton(R.string.alright) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    private fun isSupportedVendor() : Boolean {
        val manufactures = listOf("samsung","xiaomi","oneplus","huawei")
        return manufactures.contains(Build.MANUFACTURER.toLowerCase(Locale.ROOT))
    }
}