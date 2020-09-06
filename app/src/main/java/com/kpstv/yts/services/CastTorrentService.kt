package com.kpstv.yts.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.receivers.CommonBroadCast
import com.kpstv.yts.ui.helpers.MainCastHelper
import java.io.File

@SuppressLint("WakelockTimeout")
class CastTorrentService : IntentService("blank") {

    companion object {
        const val EXTRA_TORRENT = "extra_torrent_model"
        const val EXTRA_SUBTITLE_PATH = "extra_subtitle_path"
    }

    private val TAG = javaClass.simpleName
    private var wakeLock: PowerManager.WakeLock? = null
    private val CAST_NOTIFICATION_ID = 24
    private lateinit var notification: Notification
    private lateinit var torrent: Torrent
    private lateinit var torrentStream: TorrentStream
    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private lateinit var closePendingIntent: PendingIntent
    private var lastProgress = 0f
    private var testBoolean = false
    private var isDownloadComplete = false
    private var subtitlePath: String? = null

    override fun onCreate() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:Wakelock")
        wakeLock?.acquire()

        notification = Notifications.createCastNotification(this, getString(R.string.app_name), "0%")
        notificationManagerCompat = NotificationManagerCompat.from(this)

        createClosePendingIntent()

        startForeground(CAST_NOTIFICATION_ID, notification)
        super.onCreate()

        Log.e(TAG, "Created()")
    }

    private fun createClosePendingIntent() {
        val closeIntent = Intent(this, CommonBroadCast::class.java).apply {
            action = CommonBroadCast.STOP_CAST_SERVICE
        }
        closePendingIntent = PendingIntent.getBroadcast(this, Notifications.getRandomNumberCode(), closeIntent, 0)
    }

    /**
     * Make sure we only handle first intent only.
     */
    override fun onHandleIntent(intent: Intent?) {
        Log.e(TAG, "Handling Intent()")
        if (::torrent.isInitialized) return

        torrent = intent?.getSerializableExtra(EXTRA_TORRENT) as Torrent
        subtitlePath = intent.getStringExtra(EXTRA_SUBTITLE_PATH)

        val storeLocation = File(externalCacheDir, AppInterface.STREAM_LOCATION)
        val imagePath = File(storeLocation, "banner-${torrent.title}.png")
        AppUtils.saveImageFromUrl(torrent.banner_url, imagePath)

        val torrentOptions = TorrentOptions.Builder()
            .autoDownload(true)
            .saveLocation(storeLocation)
            .removeFilesAfterStop(false)
            .anonymousMode(AppInterface.ANONYMOUS_TORRENT_DOWNLOAD)
            .build()

        torrentStream = TorrentStream.init(torrentOptions)
        torrentStream.addListener(object : TorrentListener {
            override fun onStreamStarted(p0: com.github.se_bastiaan.torrentstream.Torrent?) {
                Log.e(TAG, "=> onStreamStarted()")
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(Intent(MainCastHelper.BROADCAST_STREAM_START))
            }
            override fun onStreamStopped() {}

            override fun onStreamError(
                p0: com.github.se_bastiaan.torrentstream.Torrent?,
                p1: Exception?
            ) {
                stopSelf()
            }

            override fun onStreamPrepared(p0: com.github.se_bastiaan.torrentstream.Torrent?) {
                Log.e(TAG, "onStreamPrepared()")
            }

            override fun onStreamReady(torrent: com.github.se_bastiaan.torrentstream.Torrent?) {
                Log.e(TAG, "onStreamReady()")

                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(Intent(MainCastHelper.BROADCAST_STREAM_READY).apply {
                        putExtra(MainCastHelper.ARG_MEDIA_FILE_PATH, torrent?.videoFile?.path)
                        putExtra(MainCastHelper.ARG_BANNER_FILE_PATH, imagePath.path)
                        putExtra(MainCastHelper.ARG_SRT_FILE_PATH, subtitlePath)
                    })
            }

            override fun onStreamProgress(
                p0: com.github.se_bastiaan.torrentstream.Torrent?,
                status: StreamStatus?
            ) {
                if (status == null) return

                if (lastProgress != status.progress) {
                    lastProgress = status.progress

                    if (lastProgress > 99)
                        isDownloadComplete = true

                    Log.e(TAG, "onStreamProgress() => ${status.progress}")
                    notification = Notifications.createCastNotification(
                        context = applicationContext,
                        movieName = torrent.title,
                        progress = "${"%.2f".format(lastProgress)}%",
                        closePendingIntent = closePendingIntent
                    )
                    notificationManagerCompat.notify(CAST_NOTIFICATION_ID, notification)
                }
            }
        })
        torrentStream.startStream(torrent.url)
        do {
            testBoolean = true
           // Log.e(TAG, "Running service...")
        } while (!isDownloadComplete)
    }

    override fun onDestroy() {
        Log.e(TAG, "Destroyed()")
        notificationManagerCompat.cancel(CAST_NOTIFICATION_ID)

        wakeLock?.release()
        if (::torrentStream.isInitialized)
            torrentStream.stopStream()
        super.onDestroy()
    }
}