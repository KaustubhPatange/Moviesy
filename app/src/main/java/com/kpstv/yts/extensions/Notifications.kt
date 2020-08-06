package com.kpstv.yts.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.core.app.NotificationCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.data.models.AppDatabase
import com.kpstv.yts.ui.activities.SplashActivity
import java.util.*

object Notifications {

    const val UPDATE_REQUEST_CODE = 129

    private lateinit var mgr: NotificationManager

    fun setup(context: Context) = with(context) {
        mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (SDK_INT >= 26) {
            val channel = NotificationChannel(
                getString(R.string.CHANNEL_ID_2),
                context.getString(R.string.download),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mgr.createNotificationChannel(channel)
        }
    }

    fun sendUpdateNotification(context: Context, update: AppDatabase.Update) = with(context) {
        val updateIntent = Intent(this, SplashActivity::class.java)
            .apply {
                putExtra(AppInterface.UPDATE_URL, update.url)
            }
        val pendingIntent = PendingIntent.getActivity(
            context,
            UPDATE_REQUEST_CODE,
            updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, getString(R.string.CHANNEL_ID_2))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.update_message))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()

        mgr.notify(getRandomNotificationId(), notification)
    }

    private fun getRandomNotificationId() = Random().nextInt(400) + 150
}