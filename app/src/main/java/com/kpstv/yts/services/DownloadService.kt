package com.kpstv.yts.services

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.text.Html
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.kpstv.yts.AppInterface.Companion.ANONYMOUS_TORRENT_DOWNLOAD
import com.kpstv.yts.AppInterface.Companion.DOWNLOAD_CONNECTION_TIMEOUT
import com.kpstv.yts.AppInterface.Companion.DOWNLOAD_TIMEOUT_SECOND
import com.kpstv.yts.AppInterface.Companion.EMPTY_QUEUE
import com.kpstv.yts.AppInterface.Companion.MODEL_UPDATE
import com.kpstv.yts.AppInterface.Companion.PAUSE_JOB
import com.kpstv.yts.AppInterface.Companion.PENDING_JOB_UPDATE
import com.kpstv.yts.AppInterface.Companion.REMOVE_CURRENT_JOB
import com.kpstv.yts.AppInterface.Companion.REMOVE_JOB
import com.kpstv.yts.AppInterface.Companion.STOP_SERVICE
import com.kpstv.yts.AppInterface.Companion.STORAGE_LOCATION
import com.kpstv.yts.AppInterface.Companion.TORRENT_NOT_SUPPORTED
import com.kpstv.yts.AppInterface.Companion.formatDownloadSpeed
import com.kpstv.yts.R
import com.kpstv.yts.data.db.repository.DownloadRepository
import com.kpstv.yts.data.db.repository.PauseRepository
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getVideoDuration
import com.kpstv.yts.extensions.utils.AppUtils.Companion.saveImageFromUrl
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.data.models.TorrentJob
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.receivers.CommonBroadCast
import com.kpstv.yts.ui.activities.DownloadActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@SuppressLint("WakelockTimeout")
@AndroidEntryPoint
class DownloadService: IntentService("blank") {

    @Inject lateinit var pauseRepository: PauseRepository
    @Inject lateinit var downloadRepository: DownloadRepository

    val TAG = "DownloadService"
    private var pendingJobs = ArrayList<Torrent>()
    private var currentModel: com.github.se_bastiaan.torrentstream.Torrent? = null
    private var currentTorrentModel: Torrent? = null
    private lateinit var context: Context
    private var wakeLock: WakeLock? = null
    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private lateinit var contentIntent: PendingIntent
    private lateinit var cancelIntent: PendingIntent
    private lateinit var torrentStream: TorrentStream
    private val FOREGROUND_ID = 1
    private var toDelete = false
    private var totalGap: Long? = 0
    private var lastProgress: Float? = 0f

    private val SHOW_LOG_FROM_THIS_CLASS = true

    init {
        setIntentRedelivery(true)
    }

    override fun onCreate() {

        DS_LOG("==> onCreate() called")

        context = applicationContext

        notificationManagerCompat = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.CHANNEL_ID_1),
                "ytsdownload",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Movie Download Service"
            val notificationManager: NotificationManager? =
                context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)

            val channel = NotificationChannel(
                getString(R.string.CHANNEL_ID_2),
                "Download",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(channel)
        }

        val newIntent = Intent(context, CommonBroadCast::class.java)
        newIntent.action = STOP_SERVICE
        cancelIntent = PendingIntent.getBroadcast(context, 5, newIntent, 0)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:Wakelock")
        wakeLock?.acquire()

        /** Registering local broadcast receiver */

        val filter = IntentFilter(REMOVE_JOB)
        filter.addAction(REMOVE_CURRENT_JOB)
        filter.addAction(PAUSE_JOB)
        LocalBroadcastManager.getInstance(context).registerReceiver(localBroadcastReceiver, filter)

        super.onCreate()
    }

    private fun setContentIntent(config: Torrent, torrentJob: TorrentJob? = null) {
        val notificationIntent = Intent(context, DownloadActivity::class.java)
        val t: TorrentJob = torrentJob
            ?: TorrentJob(
                config.title, config.banner_url, 0, 0, 0f, 0, 0, false, "Pending",
                0, getMagnetHash(config.url)
            )
        notificationIntent.action = MODEL_UPDATE
        notificationIntent.putExtra("model", t)
        contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config: Torrent = intent!!.getSerializableExtra("addJob") as Torrent
        pendingJobs.add(config)

        DS_LOG("==> onStartCommand(): ${config.title}")

        setContentIntent(config)

        val notification = NotificationCompat.Builder(context, getString(R.string.CHANNEL_ID_1))
            .setContentTitle(getContentTitle(config.title, 0))
            .addAction(R.mipmap.ic_launcher, "Cancel", cancelIntent)
            .setContentText(getContentText("0 KB/s"))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(contentIntent)
            .setOngoing(true)
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

        var isJobCompleted = false
        var isConnected = false
        var isError = false
        var isStopped = false
        var isCurrentProgressUpdated = false
        var isAutoStopped = false
        toDelete = false

        val model = intent?.getSerializableExtra("addJob") as Torrent

        currentTorrentModel = model

        /** Removing pauseModel from database if current model is already a pause Job */

        pauseRepository.deletePause(model.hash)

        DS_LOG("==> onHandleIntent(): ${model.title}")

        var isExist = false
        for (c in pendingJobs) {
            if (c.title == model.title) isExist = true
        }

        if (!isExist) return

        if (pendingJobs.size > 0)
            pendingJobs.removeAt(0)

        /** Update pending jobs */
        updatePendingJobs()

        val torrentOptions: TorrentOptions = TorrentOptions.Builder()
            .saveLocation(STORAGE_LOCATION)
            .autoDownload(true)
            .removeFilesAfterStop(false)
            .anonymousMode(ANONYMOUS_TORRENT_DOWNLOAD)
            .build()

        updateNotification(model, null, true)
        DS_LOG("==> Loading Job: ${model.title}")

        torrentStream = TorrentStream.init(torrentOptions)
        torrentStream.addListener(object : TorrentListener {
            override fun onStreamReady(torrent: com.github.se_bastiaan.torrentstream.Torrent?) {
                DS_LOG("==> Stream Ready")
            }

            override fun onStreamPrepared(torrent: com.github.se_bastiaan.torrentstream.Torrent?) {
                updateNotification(model, torrent, true)
                currentModel = torrent
                DS_LOG("==> Preparing Job: ${torrent?.saveLocation}")
                lastProgress = 0f
                totalGap = getCurrentTimeSecond()
                isConnected = true
            }

            override fun onStreamStopped() {
                DS_LOG("==> Stream Stopped")
                if (currentModel != null && toDelete) {
                    AppUtils.deleteRecursive(currentModel?.saveLocation)
                }
                isStopped = true
                isJobCompleted = true
            }

            override fun onStreamStarted(torrent: com.github.se_bastiaan.torrentstream.Torrent?) {
                DS_LOG("==> Stream Started")
            }

            override fun onStreamProgress(
                torrent: com.github.se_bastiaan.torrentstream.Torrent?,
                status: StreamStatus?
            ) {
                if (lastProgress == 0f || lastProgress != status?.progress) {
                    updateNotification(model, torrent, false, status)
                    DS_LOG(
                        "==> Progress: ${status?.progress}, Download queue: ${torrent?.torrentHandle?.downloadQueue?.size}, " +
                                "Piece availability: ${torrent?.torrentHandle?.pieceAvailability?.size}, " +
                                "Piece size: ${torrent?.torrentHandle?.torrentFile()
                                    ?.numPieces()}, " +
                                "Pieces to prepare: ${torrent?.piecesToPrepare}"
                    )
                    if (status?.progress?.toInt() == 100) {
                        isJobCompleted = true
                        DS_LOG("==> JobCompleted")
                    }
                }
                totalGap = getCurrentTimeSecond()
                isCurrentProgressUpdated = true
                lastProgress = status?.progress
            }

            override fun onStreamError(
                torrent: com.github.se_bastiaan.torrentstream.Torrent?,
                e: Exception?
            ) {
                DS_LOG("==> Stream Error")
                isJobCompleted = true
                isError = true
            }
        })

        torrentStream.startStream(model.url)

        do {
            if ((isCurrentProgressUpdated && getCurrentTimeSecond() > totalGap!! + DOWNLOAD_TIMEOUT_SECOND && lastProgress?.toInt()!! >= 98)
                || (isConnected && getCurrentTimeSecond() > totalGap!! + DOWNLOAD_CONNECTION_TIMEOUT && lastProgress?.toInt()!! == 0)
            ) {
                DS_LOG("==> Auto Stopping Stream")
                toDelete = false
                isAutoStopped = true
                torrentStream.stopStream()

                if (lastProgress == 0f) {
                    val i = Intent(context, CommonBroadCast::class.java)
                    i.action = TORRENT_NOT_SUPPORTED
                    sendBroadcast(i)
                    stopSelf()
                }
            }
        } while (!isJobCompleted)

        if (!isStopped || isAutoStopped) handleAfterJobComplete(model, isError)

        onClear()
    }

    private fun onClear() {
        currentModel = null
        currentTorrentModel = null
    }

    fun updateNotification(
        model: Torrent, torrent: com.github.se_bastiaan.torrentstream.Torrent?
        , isIndeterminate: Boolean, status: StreamStatus? = null
    ) {

        /** Prepare current model */

        val torrentJob: TorrentJob
        val magnetHash: String = getMagnetHash(model.url)

        if (status != null) {

            /** No passing of currentSize since torrent downloads usually create a skeleton structure
             *  with total torrent size and then update byte location provided by torrent pieces
             *
             *  val currentSize = Utils.getDirSize(torrent?.saveLocation!!)
             */

            torrentJob = TorrentJob(
                model.title,
                model.banner_url,
                status.progress.toInt(),
                status.seeds,
                status.downloadSpeed,
                0,
                torrent?.torrentHandle?.torrentFile()?.totalSize(),
                true,
                "Downloading",
                torrent?.torrentHandle?.peerInfo()?.size as Int,
                magnetHash
            )
        } else {
            var size = torrent?.torrentHandle?.torrentFile()?.totalSize()
            size ?: kotlin.run { size = 0 }
            torrentJob = TorrentJob(
                model.title, model.banner_url, 0, 0, 0f, 0,
                size, false, "Preparing", 0, magnetHash
            )
        }


        /** Update the notification channel */

        /*var current = status?.progress?.toInt()
        current ?: kotlin.run { current = 0 }*/

        var speed = status?.downloadSpeed
        speed ?: kotlin.run { speed = 0f }

        var progress = status?.progress
        progress ?: kotlin.run { progress = 0f }

        setContentIntent(model, torrentJob)

        val speedString = formatDownloadSpeed(speed as Float)

        val notificationBuilder =
            NotificationCompat.Builder(context, getString(R.string.CHANNEL_ID_1))
                .setContentTitle(getContentTitle(model.title, progress?.toInt()))
                .addAction(R.mipmap.ic_launcher, "Cancel", cancelIntent)
                .setContentText(getContentText(speedString))
                .setSmallIcon(android.R.drawable.stat_sys_download) //TODO: Create your own yts app icon and set it here
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)

        if (isIndeterminate)
            notificationBuilder.setProgress(100, 0, true)

        notificationManagerCompat.notify(FOREGROUND_ID, notificationBuilder.build())

        /** Update the current model */

        val intent = Intent(MODEL_UPDATE)
        intent.putExtra("model", torrentJob)
        intent.putExtra("models", pendingJobs)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun getContentText(speedString: String): String {
        return "Downloading: ${pendingJobs.size + 1}  ${Html.fromHtml("&#8226;")}  $speedString ${Html.fromHtml(
            "&#8595;"
        )}"
    }

    private fun getContentTitle(title: String?, progress: Int?): String {
        return "$title  ${Html.fromHtml("&#8226;")}   ${progress}%"
    }


    private fun getMagnetHash(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }

    private fun updatePendingJobs() {
        val intent = Intent(PENDING_JOB_UPDATE)
        intent.putExtra("models", pendingJobs)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun updateEmptyQueue() {
        val intent = Intent(EMPTY_QUEUE)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun handleAfterJobComplete(model: Torrent, isError: Boolean = false) {

        /** Create notification based on error bool */

        val not: Notification
        val contentText = model.title
        if (!isError) {

            /** Save details of file in database.
             */

            val imagePath = File(currentModel?.saveLocation, "banner.png")
            saveImageFromUrl(model.banner_url, imagePath)

            val todayDate = SimpleDateFormat("yyyy-MM-dd")
                .format(Calendar.getInstance().time)

            val movieSize = getVideoDuration(this, currentModel?.videoFile!!)
                .takeUnless { it == null } ?: 0L

            downloadRepository.saveDownload(
                Model.response_download(
                    title = model.title,
                    imagePath = imagePath.path,
                    downloadPath = currentModel?.saveLocation?.path,
                    size = model.size,
                    date_downloaded = todayDate,
                    hash = model.hash,
                    total_video_length = movieSize,
                    videoPath = currentModel?.videoFile?.path,
                    movieId = currentTorrentModel?.movieId,
                    imdbCode = currentTorrentModel?.imdbCode
                )
            )

            // TODO: Set content intent to do something when notification is clicked

            not = NotificationCompat.Builder(context, getString(R.string.CHANNEL_ID_2)).apply {
                setContentTitle("Download Complete")
                setContentText(contentText)
                setSmallIcon(R.drawable.ic_check)
                setAutoCancel(true)
                priority = Notification.PRIORITY_LOW
            }.build()
            /* .setContentTitle("Download Complete")
             .setContentText(contentText)
             .setSmallIcon(R.drawable.ic_check)
           //  .setContentIntent(opensongService)
             .setAutoCancel(true)
             .setPriority(Notification.PRIORITY_LOW)
             //.addAction(R.mipmap.ic_launcher, "Share", openshareService)
             .build()*/
        } else {
            not = NotificationCompat.Builder(context, getString(R.string.CHANNEL_ID_2)).apply {
                setDefaults(Notification.DEFAULT_ALL)
                setContentTitle("Download Failed")
                setContentText(contentText)
                setSmallIcon(R.drawable.ic_error_outline)
                setAutoCancel(true)
                priority = Notification.PRIORITY_LOW
            }.build()
        }

        /** Throw notification with random id */

        val nos: Int = Random().nextInt(400) + 150
        notificationManagerCompat.notify(nos, not)
    }

    private val localBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                PAUSE_JOB -> {
                    val torrentJob = intent.getSerializableExtra("model") as TorrentJob
                    torrentJob.status = "Paused"
                    val modelPause = Model.response_pause(
                        job = torrentJob,
                        hash = torrentJob.magnetHash,
                        torrent = currentTorrentModel,
                        saveLocation = currentModel?.saveLocation?.path
                    )

                    /** Saving current job to database and making it pause*/

                    pauseRepository.savePauseModel(
                        modelPause
                    )

                    /** Removing current job */

                    torrentStream.stopStream()
                }
                /* UNPAUSE_JOB_NORMAL -> {
                     val modelJob = intent.getSerializableExtra("model") as Model.response_pause
                     pendingJobs.add(modelJob.torrent!!)
                     PauseRepository(MainDatabase.invoke(applicationContext)).deletePause(modelJob.hash)
                     updatePendingJobs()
                 }
                 UNPAUSE_JOB_TOP -> {
                     val modelJob = intent.getSerializableExtra("model") as Model.response_pause
                     pendingJobs.add(0, currentTorrentModel!!)
                     pendingJobs.add(0, modelJob.torrent!!)
                     torrentStream.stopStream()
                     PauseRepository(MainDatabase.invoke(applicationContext)).deletePause(modelJob.hash)
                     updatePendingJobs()
                 }*/
                REMOVE_JOB -> {
                    val model: Torrent = intent.extras?.getSerializable("model") as Torrent
                    for (c in pendingJobs) {
                        if (model.title == c.title) {
                            pendingJobs.remove(c)
                            DS_LOG("OK it is removed")
                            break
                        }
                    }
                }
                REMOVE_CURRENT_JOB -> {
                    toDelete = intent.getBooleanExtra("deleteFile", false)
                    torrentStream.stopStream()
                }
            }
        }
    }

    fun getCurrentTimeSecond(): Long {
        return System.currentTimeMillis() / 1000
    }

    override fun onDestroy() {

        DS_LOG("==> onDestroy() called")

        /** Update receiver to know that all jobs completed */
        updateEmptyQueue()

        /** Remove registered localbroadcast */
        LocalBroadcastManager.getInstance(context).unregisterReceiver(localBroadcastReceiver)

        pendingJobs.clear()
        if (torrentStream.isStreaming) torrentStream.stopStream()
        wakeLock?.release()
        notificationManagerCompat.cancel(FOREGROUND_ID);

        super.onDestroy()
    }

    private fun DS_LOG(message: String) {
        if (SHOW_LOG_FROM_THIS_CLASS)
            Log.e(TAG, message)
    }


}