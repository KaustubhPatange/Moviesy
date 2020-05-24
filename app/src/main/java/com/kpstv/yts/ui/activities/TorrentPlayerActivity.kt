package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.kpstv.yts.AppInterface.Companion.ANONYMOUS_TORRENT_DOWNLOAD
import com.kpstv.yts.AppInterface.Companion.STREAM_LOCATION
import com.kpstv.yts.AppInterface.Companion.SUBTITLE_LOCATION
import com.kpstv.yts.R
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.models.SubHolder
import com.kpstv.yts.utils.SubtitleUtils
import com.kpstv.yts.data.viewmodels.MainViewModel
import com.kpstv.yts.data.viewmodels.providers.MainViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_torrent_player.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import tcking.github.com.giraffeplayer2.GiraffePlayer
import tcking.github.com.giraffeplayer2.PlayerListener
import tcking.github.com.giraffeplayer2.VideoInfo
import tv.danmaku.ijk.media.player.IjkTimedText
import java.io.File

/** @Usage
 *
 *  Pass "torrentLink" and "sub" filePath (optional)
 *  Pass "normalLink" and "sub" filePath (optional)
 *
 */
@SuppressLint("SetTextI18n")
class TorrentPlayerActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: MainViewModelFactory by instance()

    private val TAG = "TorrentPlayerActivity"
    private lateinit var torrentStream: TorrentStream
    private var lastProgress = 0f
    private lateinit var subHolders: ArrayList<SubHolder>
    private var subtitleHandler = Handler()
    private var models: ArrayList<SubHolder> = ArrayList()
    private var lastSubtitlePosition: Int = 0
    private lateinit var viewModel: MainViewModel
    private lateinit var hash: String
    private var hasSubtitle: Boolean = false
    private var isTorrentLink: Boolean = false
    private var isLoadedfromLast: Boolean = false

    private var lastPausePosition: Int? = 0
    private var filePath: String? = null


    override fun onPause() {
        Log.e(TAG, "==> onPause() called")
        try {

            if (filePath == null) return

            /** Here we are saving current player position to database,
             *  so that we can retrieve and show Play from checkbox in bottom sheet.
             */
            val position = giraffe_player.player.currentPosition
            Log.e(TAG, "==> LastSavedPosition: $position")
            if (::viewModel.isInitialized) {
                viewModel.updateDownload(
                    hash, true, position
                )
            }

            /**  We will save current player position to last pause position,
             *   so in onResume we can retrieve it.
             *   We are also releasing the player since it still plays in
             *   background even after pausing the video.
             */
            lastPausePosition = giraffe_player.player.currentPosition

            giraffe_player.player.release()

            /** We will also remove subtitle handler callbacks, which does
             *  automatically triggers play() on player during syncing.
             */
            if (hasSubtitle) subtitleHandler.removeCallbacks(updateTask)
        } catch (e: Exception) { }
        super.onPause()
    }

    override fun onResume() {
        Log.e(TAG, "onResume() called $lastPausePosition")
        try {

            if (filePath == null) return

            /** Since player is destroyed in onPause() we will recreate
             *  player instance. It will automatically sync it to
             *  last pause position.
             */
            startPlayer(File(filePath), File(filePath).name)
        } catch (e: Exception) { }
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_torrent_player)

        tf_subtitle.text = ""

        /** Here we retrieve last saved position from database (if exist).
         */
        intent.getIntExtra("lastPosition", 0).let {
            lastPausePosition = it
            lastSubtitlePosition = it
            isLoadedfromLast = true
        }

        /** Check if the callback is for torrentLink and recreate that methods.
         */
        intent.getStringExtra("torrentLink")?.let { torrentLink ->
            torrentSpecific(torrentLink)
        } ?: 
            /** Check if the callback is for normalLink i.e playing from local storage.
             */
        intent.getStringExtra("normalLink")?.let { filePath ->
            this.filePath = filePath
            progressText.hide()
            hash = intent.getStringExtra("hash")!!

            viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

            val file = File(filePath)
            startPlayer(
                file,
                file.name
            )
        } ?: Toasty.error(this, "Could not figure out the type").show()

        /** Getting subtitle local path and parsing subtitles.
         */
        intent?.getStringExtra("sub")?.let { filename ->
            hasSubtitle = true
            subHolders = SubtitleUtils.parseSubtitles(File(SUBTITLE_LOCATION, filename).path)
        }

        if (!::subHolders.isInitialized)
            tf_subtitle.visibility = View.GONE
        else {
            /** If this has subtitles we will start a handler which will show the subtitles.
             */
            handlerHandler()
            subtitleHandler.postDelayed(updateTask, 500)
        }
    }

    /** This function is used to stream torrent files in external cache dir.
     */
    private fun torrentSpecific(link: String) {
        val storeLocation = File(externalCacheDir, STREAM_LOCATION)

        val torrentOptions = TorrentOptions.Builder()
            .autoDownload(true)
            .saveLocation(storeLocation)
            .removeFilesAfterStop(false)
            .anonymousMode(ANONYMOUS_TORRENT_DOWNLOAD)
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
                Toasty.error(this@TorrentPlayerActivity, "Torrent stream error: ${e.message}").show()
            }

            override fun onStreamReady(torrent: Torrent) {
                Log.e(TAG, "onStreamReady() called with: torrent = [$torrent]")

                /** Stream ready so start the player from here.
                 */
                filePath = torrent.videoFile.path
                startPlayer(torrent.videoFile, torrent.videoFile.name)
            }

            override fun onStreamProgress(torrent: Torrent, status: StreamStatus) {
                if (lastProgress != status.progress) {
                    Log.e(TAG, "onStreamProgress() ==> ${status.progress}")
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
        torrentStream.startStream(link)
    }

    /** A common function to load player
     */
    private fun startPlayer(file: File, title: String) {
        progressBar.visibility = View.GONE

        val uri = Uri.fromFile(file)
        val videoInfo = VideoInfo(uri)
            .setTitle(title)
            .setShowTopBar(true)
            .setAspectRatio(VideoInfo.AR_ASPECT_FILL_PARENT)
            .setBgColor(
                ContextCompat.getColor(
                    this@TorrentPlayerActivity,
                    android.R.color.black
                )
            )

        giraffe_player.playerListener = listener

        giraffe_player.videoInfo(videoInfo).player.start()
    }

    /** A prefetch for subtitle Handler is done here.
     *  It is also called when there is a change in player seek.
     */
    private fun handlerHandler() {
        Log.e(TAG, "handlerHandler() called")
        tf_subtitle.text = ""
        if (lastSubtitlePosition == 0)
            models = ArrayList(subHolders)
        else {
            models.clear()

            val position = giraffe_player.player.currentPosition

            subHolders.forEach {
                if (it.startTime >= position) {
                    models.add(it)
                }
            }

            Log.e(TAG, "SubHolderSize: ${subHolders.size}, Model size: ${models.size}")
        }
    }

    /** A handler which will show subtitle. A code which works perfect but now
     *  I am too lazy to understand it again.
     */
    var subShowing = false
    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            try {
                if (giraffe_player.isCurrentActivePlayer) {
                    val currentPosition = giraffe_player.player!!.currentPosition

                    if (currentPosition < lastSubtitlePosition || currentPosition > lastSubtitlePosition + 7 * 1000) {
                        /** Player either seekTo backwards  || currentPosition > lastPostion + 1000*/

                        Log.e(TAG, "==> Player seekTo change")
                        subShowing = false
                        giraffe_player.player.pause()
                        handlerHandler()
                        if (noPlayerStartHandler) {
                            noPlayerStartHandler = false
                        }else giraffe_player.player.start()
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
                    lastSubtitlePosition = currentPosition
                }
                subtitleHandler.postDelayed(this, 100)
            } catch (e: Exception) {
                Toasty.error(this@TorrentPlayerActivity, "Subtitle crashed due to: ${e.message}")
                    .show()
                subtitleHandler.removeCallbacks(this)
            }
        }
    }

    override fun onDestroy() {
        try {
            giraffe_player.player.release()
        } catch (e: Exception) {
            Log.e(TAG, "==> Error: ${e.message}")
        }
        subtitleHandler.removeCallbacks(updateTask)
        if (::torrentStream.isInitialized) torrentStream.stopStream()
        super.onDestroy()
    }

    /** A callback to manage player. This will also seek the player to
     *  last saved position if not 0.
     */
    private var noPlayerStartHandler = false
    val listener = object: PlayerListener {
        override fun onTimedText(giraffePlayer: GiraffePlayer?, text: IjkTimedText?) {

        }

        override fun onPrepared(giraffePlayer: GiraffePlayer?) {
            Log.e(TAG, "==> onPrepared $lastPausePosition")
            giraffePlayer?.seekTo(lastPausePosition!!)
        }

        override fun onRelease(giraffePlayer: GiraffePlayer?) {
        }

        override fun onCompletion(giraffePlayer: GiraffePlayer?) {
        }

        override fun onPause(giraffePlayer: GiraffePlayer?) {
        }

        override fun onLazyLoadError(giraffePlayer: GiraffePlayer?, message: String?) {
        }

        override fun onTargetStateChange(oldState: Int, newState: Int) {
        }

        override fun onDisplayModelChange(oldModel: Int, newModel: Int) {
        }

        override fun onSeekComplete(giraffePlayer: GiraffePlayer?) {
            Log.e(TAG, "==> SeekChange() ")
            if (lastPausePosition  != 0 && !isLoadedfromLast) {
                giraffePlayer?.pause()
                noPlayerStartHandler = true
                if (hasSubtitle) subtitleHandler.postDelayed(updateTask, 100)
            }

            isLoadedfromLast = false

            /** I had to do this hacks since the Player doesn't have removeListener method
             */
            giraffe_player.playerListener = object: PlayerListener {
                override fun onTimedText(giraffePlayer: GiraffePlayer?, text: IjkTimedText?) {
                }

                override fun onPrepared(giraffePlayer: GiraffePlayer?) {
                }

                override fun onRelease(giraffePlayer: GiraffePlayer?) {
                }

                override fun onCompletion(giraffePlayer: GiraffePlayer?) {
                }

                override fun onPause(giraffePlayer: GiraffePlayer?) {
                }

                override fun onLazyLoadError(giraffePlayer: GiraffePlayer?, message: String?) {
                }

                override fun onTargetStateChange(oldState: Int, newState: Int) {
                }

                override fun onDisplayModelChange(oldModel: Int, newModel: Int) {
                }

                override fun onSeekComplete(giraffePlayer: GiraffePlayer?) {
                }

                override fun onInfo(giraffePlayer: GiraffePlayer?, what: Int, extra: Int): Boolean {
                    return false
                }

                override fun onBufferingUpdate(giraffePlayer: GiraffePlayer?, percent: Int) {
                }

                override fun onCurrentStateChange(oldState: Int, newState: Int) {
                }

                override fun onLazyLoadProgress(giraffePlayer: GiraffePlayer?, progress: Int) {
                }

                override fun onError(
                    giraffePlayer: GiraffePlayer?,
                    what: Int,
                    extra: Int
                ): Boolean {
                    return false
                }

                override fun onPreparing(giraffePlayer: GiraffePlayer?) {
                }

                override fun onStart(giraffePlayer: GiraffePlayer?) {
                }
            }
        }

        override fun onInfo(giraffePlayer: GiraffePlayer?, what: Int, extra: Int): Boolean {
            Log.e(TAG, "==> Info() What = $what & Extra = $extra")
            return true
        }

        override fun onBufferingUpdate(giraffePlayer: GiraffePlayer?, percent: Int) {
        }

        override fun onCurrentStateChange(oldState: Int, newState: Int) {
        }

        override fun onLazyLoadProgress(giraffePlayer: GiraffePlayer?, progress: Int) {
        }

        override fun onError(giraffePlayer: GiraffePlayer?, what: Int, extra: Int): Boolean {
            Log.e(TAG, "==> OnError() What = $what & Extra = $extra")
            return true
        }

        override fun onPreparing(giraffePlayer: GiraffePlayer?) {
        }

        override fun onStart(giraffePlayer: GiraffePlayer?) {
        }
    }
}