package com.kpstv.yts.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.kpstv.yts.AppInterface.Companion.STREAM_LOCATION
import com.kpstv.yts.AppInterface.Companion.SUBTITLE_LOCATION
import com.kpstv.yts.R
import com.kpstv.yts.models.SubHolder
import com.kpstv.yts.utils.SubtitleUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_torrent_player.*
import tcking.github.com.giraffeplayer2.GiraffePlayer
import tcking.github.com.giraffeplayer2.PlayerListener
import tcking.github.com.giraffeplayer2.VideoInfo
import tv.danmaku.ijk.media.player.IjkTimedText
import java.io.File
import java.util.stream.Collectors.toCollection
import kotlin.math.log

@SuppressLint("SetTextI18n")
class TorrentPlayerActivity : Activity() {

    private val TAG = "TorrentPlayerActivity"
    private lateinit var torrentStream: TorrentStream
    private var lastProgress = 0f
    private lateinit var subHolders: ArrayList<SubHolder>
    private var subtitleHandler = Handler()
    private var models: ArrayList<SubHolder> = ArrayList()
    private var lastPostion: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        setContentView(R.layout.activity_torrent_player)

        tf_subtitle.text = ""

        val storeLocation = File(externalCacheDir,STREAM_LOCATION)

        val torrentOptions = TorrentOptions.Builder()
            .saveLocation(storeLocation)
            .build()

        torrentStream = TorrentStream.init(torrentOptions)
        torrentStream.addListener(object : TorrentListener {
            override fun onStreamPrepared(torrent: Torrent) {
                Log.e(TAG, "onStreamPrepared() called with: torrent = [$torrent]")
            }

            override fun onStreamStarted(torrent: Torrent) {
                Log.e(TAG, "onStreamStarted() called with: torrent = [$torrent]")
            }

            override fun onStreamError(torrent: Torrent, e: Exception) {
                Log.e(TAG, "onStreamError() called with: torrent = [$torrent], e = [$e]")
            }

            override fun onStreamReady(torrent: Torrent) {
                Log.e(TAG, "onStreamReady() called with: torrent = [$torrent]")

                progressBar.visibility = View.GONE

                val uri = Uri.fromFile(torrent.videoFile)
                val videoInfo = VideoInfo(uri)
                    .setTitle(torrent.videoFile.name)
                    .setShowTopBar(true)
                    .setAspectRatio(VideoInfo.AR_ASPECT_FILL_PARENT)
                    .setBgColor(ContextCompat.getColor(this@TorrentPlayerActivity,android.R.color.black))

                giraffe_player.videoInfo(videoInfo).player.start()

            }

            override fun onStreamProgress(torrent: Torrent, status: StreamStatus) {
                if (lastProgress!=status.progress) {
                    lastProgress = status.progress
                    progressText.text = "${lastProgress.toInt()}%"
                }
                if (lastProgress.toInt() >= 98) {
                    progressText.visibility = View.GONE
                }
            }

            override fun onStreamStopped() {
                Log.d(TAG, "onStreamStopped() called")
            }
        })
        torrentStream.startStream(intent.getStringExtra("torrentLink"))

        intent?.getStringExtra("sub")?.let {filename ->
            subHolders = SubtitleUtils.parseSubtitles(File(SUBTITLE_LOCATION,filename).path)
        }

        if (!::subHolders.isInitialized) tf_subtitle.visibility = View.GONE
        else {
            handlerHandler()
            subtitleHandler.postDelayed(updateTask,500)
        }
    }

    private fun handlerHandler() {
        Log.e(TAG, "handlerHandler() called")
        tf_subtitle.text = ""
        if (lastPostion==0)
            models = ArrayList(subHolders)
        else {
            models.clear()

            val position = giraffe_player.player.currentPosition

            subHolders.forEach {
                if (it.startTime >= position)
                {
                    models.add(it)
                }
            }

            Log.e(TAG, "SubHolderSize: ${subHolders.size}, Model size: ${models.size}")
        }
    }

    var subShowing=false
    private val updateTask: Runnable = object: Runnable {
        override fun run() {
            try {
                if (giraffe_player.isCurrentActivePlayer) {
                    val currentPosition = giraffe_player.player!!.currentPosition

                    if (currentPosition < lastPostion || currentPosition > lastPostion + 7 * 1000) {
                        /** Player either seekTo backwards  || currentPosition > lastPostion + 1000*/

                        Log.e(TAG, "==> Player seekTo change")
                        subShowing = false
                        giraffe_player.player.pause()
                        handlerHandler()
                        giraffe_player.player.start()
                    }
                    if (models.isNotEmpty()) {
                        if (!subShowing) {
                            if (currentPosition >= models[0].startTime) {
                                tf_subtitle.text = models[0].text
                                subShowing = true
                            }
                        } else if (subShowing) {
                            if (currentPosition >= models[0].endTime) {
                                tf_subtitle.text = ""
                                models.removeAt(0)
                                subShowing = false
                            }
                        }
                    }
                    lastPostion = currentPosition
                }
                subtitleHandler.postDelayed(this, 100)
            }catch (e: Exception) {
                Toasty.error(this@TorrentPlayerActivity,"Subtitle crashed due to: ${e.message}").show()
            }finally {
                subtitleHandler.removeCallbacks(this)
            }
        }
    }

    override fun onDestroy() {
        giraffe_player.player.release()
        subtitleHandler.removeCallbacks(updateTask)
        torrentStream.stopStream()
        super.onDestroy()
    }
}
