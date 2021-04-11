package com.kpstv.yts.extensions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kpstv.common_moviesy.extensions.colorFrom
import com.kpstv.common_moviesy.extensions.utils.CommonUtils
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.receivers.CommonBroadCast
import com.kpstv.yts.ui.activities.FinalActivity
import com.kpstv.yts.ui.activities.SplashActivity
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.helpers.ActivityIntentHelper
import java.io.File
import java.util.*

object Notifications {

    private val TAG = javaClass.simpleName

    private const val UPDATE_REQUEST_CODE = 129
    private const val UPDATE_NOTIFICATION_ID = 7
    private const val UPDATE_PROGRESS_NOTIFICATION_ID = 21

    private lateinit var mgr: NotificationManager

    fun setup(context: Context) = with(context) {
        mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (SDK_INT >= 26) {
            mgr.createNotificationChannel(
                NotificationChannel(getString(R.string.CHANNEL_ID_2), context.getString(R.string.download), NotificationManager.IMPORTANCE_DEFAULT)
            )

            mgr.createNotificationChannel(
                NotificationChannel(getString(R.string.CHANNEL_ID_3), getString(R.string.update), NotificationManager.IMPORTANCE_LOW)
            )

            mgr.createNotificationChannel(
                NotificationChannel(getString(R.string.CHANNEL_ID_4), getString(R.string.stream), NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    fun sendUpdateProgressNotification(
        context: Context,
        progress: Progress,
        fileName: String,
        cancelRequestCode: Int
    ) = with(context) {
            val cancelIntent = Intent(this, CommonBroadCast::class.java).apply {
                action = CommonBroadCast.STOP_UPDATE_WORKER
            }
            val pendingIntent =
                PendingIntent.getBroadcast(this, cancelRequestCode, cancelIntent, 0)
            val notification = NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_3))
                .setContentTitle(fileName)
                .setContentText("${CommonUtils.getSizePretty(progress.currentBytes, false)} / ${CommonUtils.getSizePretty(progress.totalBytes)}")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setProgress(
                    100,
                    ((progress.currentBytes * 100) / progress.totalBytes).toInt(),
                    false
                )
                .setShowWhen(false)
                .addAction(R.drawable.ic_close, getString(R.string.close), pendingIntent)
                .build()

            mgr.notify(UPDATE_PROGRESS_NOTIFICATION_ID, notification)
        }

    fun removeUpdateProgressNotification() = mgr.cancel(UPDATE_PROGRESS_NOTIFICATION_ID)

    fun sendUpdateNotification(context: Context) = with(context) {
        val updateIntent = Intent(this, StartActivity::class.java)
            .apply {
                action = ActivityIntentHelper.ACTION_FORCE_CHECK_UPDATE
            }
        val pendingIntent = PendingIntent.getActivity(
            this,
            UPDATE_REQUEST_CODE,
            updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_2))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.update_message))
            .setSmallIcon(R.drawable.ic_support)
            .setColor(colorFrom(R.color.colorPrimary_New_DARK))
            .setColorized(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        mgr.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun sendMovieNotification(context: Context, movie: MovieShort, posterImage: Bitmap? = null, bannerImage: Bitmap? = null, featured: Boolean = true) = with(context) {
        Log.e(TAG, "=> Sending notification with movieId: ${movie.movieId}")
        val movieIntent = Intent(this, StartActivity::class.java).apply {
            action = ActivityIntentHelper.ACTION_LAUNCH_MOVIE
            putExtra(ActivityIntentHelper.PAYLOAD, movie.movieId)
        }
        val pendingIntent =
            PendingIntent.getActivity(this, getRandomNumberCode(), movieIntent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_2))
            .setContentTitle(movie.title)
            .setContentText("${if (featured) "Featured" else "Latest"} • ${movie.rating}/10 • ${movie.year} • ${movie.runtime} mins")
            .setSmallIcon(R.drawable.ic_movie)
            .setColor(colorFrom(R.color.colorPrimary_New_DARK))
            .setColorized(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (posterImage != null)
            notificationBuilder.setLargeIcon(posterImage)
        if (bannerImage != null)
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bannerImage))

        mgr.notify(getRandomNumberCode(), notificationBuilder.build())
    }

    fun sendDownloadCompleteNotification(context: Context, file: File) = with(context) {
        val installIntent = Intent(this, CommonBroadCast::class.java).apply {
            action = CommonBroadCast.INSTALL_APK
            putExtra(CommonBroadCast.ARGUMENT_APK_FILE, file.absolutePath)
        }

        val notification = NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_2))
            .setContentTitle(getString(R.string.update_download))
            .setContentText(getString(R.string.update_install))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(PendingIntent.getBroadcast(this, getRandomNumberCode(), installIntent, 0))
            .setAutoCancel(true)

        mgr.notify(UPDATE_NOTIFICATION_ID,  notification.build())
    }

    fun sendDownloadNotification(context: Context, contentText: String) = with(context) {
        val downloadIntent = Intent(this, StartActivity::class.java).apply {
            action = ActivityIntentHelper.ACTION_MOVE_TO_LIBRARY
        }

        val pendingIntent =
            PendingIntent.getActivity(this, getRandomNumberCode(), downloadIntent, 0)

        val notification =
            NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_2)).apply {
                setContentTitle(getString(R.string.download_complete))
                setContentText(contentText)
                setSmallIcon(R.drawable.ic_check)
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                priority = Notification.PRIORITY_LOW
            }.build()

        mgr.notify(getRandomNumberCode(), notification)
    }

    fun sendDownloadFailedNotification(context: Context, contentText: String) = with(context) {
        val notification =
            NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_2)).apply {
                setDefaults(Notification.DEFAULT_ALL)
                setContentTitle(getString(R.string.download_failed))
                setContentText(contentText)
                setSmallIcon(R.drawable.ic_error_outline)
                setAutoCancel(true)
                priority = Notification.PRIORITY_LOW
            }.build()

        mgr.notify(getRandomNumberCode(), notification)
    }

    fun sendSSLHandshakeNotification(context: Context) = with(context) {
        val notification =
            NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_2))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getString(R.string.error_ssl_handshake_title))
                .setContentText(getString(R.string.error_ssl_handshake_text))
                .setColor(colorFrom(R.color.colorPrimary_New_DARK))
                .setSmallIcon(R.drawable.ic_error_outline)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build()

        mgr.notify(getRandomNumberCode(), notification)
    }

    fun createCastNotification(
        context: Context,
        movieName: String = "Processing...",
        progress: Int = 0,
        closePendingIntent: PendingIntent? = null
    ): Notification = with(context) {
        val builder = NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID_4)).apply {
            setDefaults(Notification.DEFAULT_ALL)
            setContentTitle(movieName)
            setOngoing(true)
            setShowWhen(false)
            color = colorFrom(R.color.colorPrimary_New_DARK)
            setSmallIcon(R.drawable.ic_support)
            priority = Notification.PRIORITY_LOW
        }

        if (progress == 0)
            builder.setProgress(100, 0, true)
        else
            builder.setContentText("Streaming in background ($progress%)")

        if (closePendingIntent != null)
            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_close, getString(R.string.close), closePendingIntent
                ).build()
            )

        builder.build()
    }

    fun getRandomNumberCode() = Random().nextInt(400) + 150
}