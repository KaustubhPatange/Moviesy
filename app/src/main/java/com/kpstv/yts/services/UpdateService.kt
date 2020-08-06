package com.kpstv.yts.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.UpdateUtils

class UpdateService : IntentService("blank") {

    companion object {
        const val UPDATE_CHANNEL_ID = "channel_03"
        const val UPDATE_CHANNEL_NAME = "Update"
        const val FOREGROUND_ID = 104
    }

    private var lastProgress = 0

    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private lateinit var notification: Notification

    override fun onCreate() {
        super.onCreate()

        notificationManagerCompat = NotificationManagerCompat.from(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    UPDATE_CHANNEL_ID, UPDATE_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return super.onStartCommand(intent, flags, startId)

        notification = NotificationCompat.Builder(applicationContext, UPDATE_CHANNEL_ID)
            .setContentTitle("Downloading...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(Notification.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_ID, notification);
        } else {
            notificationManagerCompat.notify(FOREGROUND_ID, notification);
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        val updateUri = intent?.getStringExtra(UpdateUtils.UPDATE_URI) ?: return
        val fileName = updateUri.substring(updateUri.lastIndexOf("/") + 1, updateUri.length)
        /*PRDownloader.download(
            updateUri,
            applicationContext.getExternalFilesDir("")?.absolutePath,
            fileName
        )
            .build()
            .setOnProgressListener {
                val progress = ((it.currentBytes * 100) / it.totalBytes).toInt()
                if (progress != lastProgress)
                    postUpdateNotification(fileName, it.currentBytes, it.totalBytes, progress)
                lastProgress = progress
            }*/
    }

    private fun postUpdateNotification(
        fileName: String, currentBytes: Long, totalBytes: Long, progress: Int
    ) {
        notification = NotificationCompat.Builder(applicationContext, UPDATE_CHANNEL_ID)
            .setContentTitle(fileName)
            .setContentText(
                "${AppUtils.getSizePretty(
                    currentBytes,
                    false
                )} / ${AppUtils.getSizePretty(totalBytes)}"
            )
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_LOW)
            .setShowWhen(false)
            .build()

        notificationManagerCompat.notify(FOREGROUND_ID, notification)
    }
}