package com.kpstv.yts.extensions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.data.models.AppDatabase
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.services.UpdateService
import com.kpstv.yts.ui.activities.FinalActivity
import com.kpstv.yts.ui.activities.SplashActivity
import java.util.*

object Notifications {

    const val UPDATE_REQUEST_CODE = 129
    private const val UPDATE_NOTIFICATION_ID = 7
    private const val UPDATE_PROGRESS_NOTIFICATION_ID = 21

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

    fun sendUpdateProgressNotification(context: Context, progress: Progress, fileName: String) =
        with(context) {
            val notification = NotificationCompat.Builder(
                this,
                getString(R.string.CHANNEL_ID_2)
            )
                .setContentTitle(fileName)
                .setContentText(
                    "${AppUtils.getSizePretty(
                        progress.currentBytes,
                        false
                    )} / ${AppUtils.getSizePretty(progress.totalBytes)}"
                )
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setProgress(100, ((progress.currentBytes * 100)/progress.totalBytes).toInt(), false)
                .setShowWhen(false)
                .build()

            mgr.notify(UPDATE_PROGRESS_NOTIFICATION_ID, notification)
        }

    fun removeUpdateProgressNotification() = mgr
        .cancel(UPDATE_PROGRESS_NOTIFICATION_ID)

    fun sendUpdateNotification(context: Context, update: AppDatabase.Update) = with(context) {
        val updateIntent = Intent(this, SplashActivity::class.java)
            .apply {
                action = AppInterface.ACTION_UPDATE
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
            .setSmallIcon(R.drawable.ic_support)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary_New_DARK))
            .setColorized(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        mgr.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun sendMovieNotification(context: Context, movieName: String, movieId: Int) = with(context) {
        val movieIntent = Intent(this, FinalActivity::class.java)
            .apply {
                putExtra(AppInterface.MOVIE_ID, movieId)
            }
        val pendingIntent = PendingIntent.getActivity(
            context,
            UPDATE_REQUEST_CODE,
            movieIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, getString(R.string.CHANNEL_ID_2))
            .setContentTitle(getString(R.string.app_name))
            .setContentText("\"$movieName\" movie is available")
            .setSmallIcon(R.drawable.ic_movie)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary_New_DARK))
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .build()

        mgr.notify(getRandomNotificationId(), notification)
    }

    private fun getRandomNotificationId() = Random().nextInt(400) + 150
}