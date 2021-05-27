package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.common_moviesy.extensions.utils.WindowUtils
import com.kpstv.yts.AppInterface.Companion.ANONYMOUS_TORRENT_DOWNLOAD
import com.kpstv.yts.AppInterface.Companion.STREAM_LOCATION
import com.kpstv.yts.AppInterface.Companion.SUBTITLE_LOCATION
import com.kpstv.yts.R
import com.kpstv.yts.data.models.SubHolder
import com.kpstv.yts.databinding.ActivityTorrentPlayerBinding
import com.kpstv.yts.extensions.utils.SubtitleUtils
import com.kpstv.yts.ui.helpers.ContinueWatcherHelper
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import tcking.github.com.giraffeplayer2.GiraffePlayer
import tcking.github.com.giraffeplayer2.PlayerListener
import tcking.github.com.giraffeplayer2.VideoInfo
import tv.danmaku.ijk.media.player.IjkTimedText
import java.io.File

/** @Usage
 *
 *  Pass "torrentLink" and "sub" filePath (optional)
 *  Pass "normalLink" (filePath) and "sub" filePath (optional)
 *
 */
@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class TorrentPlayerActivity : AppCompatActivity() {

    companion object {
        const val ARG_TORRENT_LINK = "com.kpstv.yts.arg_torrent_link"
        const val ARG_NORMAL_LINK = "com.kpstv.yts.arg_normal_link"
        const val ARG_MOVIE_ID = "com.kpstv.yts.arg_movie_id"
        const val ARG_MOVIE_TITLE = "com.kpstv.yts.arg_movie_title"
        const val ARG_TORRENT_HASH = "com.kpstv.yts.arg_torrent_hash"
        const val ARG_LAST_SAVE_POS = "com.kpstv.yts.arg_last_save_pos"
        const val ARG_SUBTITLE_NAME = "com.kpstv.yts.arg_subtitle_name"

        const val LAST_POSITION = "com.kpstv.yts.last_position"
    }

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(ActivityTorrentPlayerBinding::inflate)

    private val TAG = "TorrentPlayerActivity"
    private lateinit var torrentStream: TorrentStream
    private var lastProgress = 0f
    private lateinit var subHolders: ArrayList<SubHolder>
    private var subtitleHandler = Handler()
    private var models: ArrayList<SubHolder> = ArrayList()
    private var lastSubtitlePosition: Int = 0

    private lateinit var hash: String
    private var hasSubtitle: Boolean = false
    private var isLoadedfromLast: Boolean = false

    private var lastPausePosition: Int = 0

    private var sleep: Boolean = false

    private var filePath: String? = null

    private var isLocal = false
    private var movieTitle: String = ""
    private var movieId: Int = 0

    private lateinit var continueWatcherHelper: ContinueWatcherHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        continueWatcherHelper = ContinueWatcherHelper(this, this)

        setContentView(binding.root)

        binding.subtitle.text = ""

        /** Here we retrieve last saved position from database (if exist).
         */
        intent.getIntExtra(ARG_LAST_SAVE_POS, 0).let {
            lastPausePosition = it
            lastSubtitlePosition = it
            isLoadedfromLast = true
        }

        savedInstanceState?.getInt(LAST_POSITION)?.let { lastPausePosition = it }

        /** Check if the callback is for torrentLink and recreate that methods.
         */
        intent.getStringExtra(ARG_TORRENT_LINK)?.let { torrentLink ->
            torrentSpecific(torrentLink)
        } ?:
        /** Check if the callback is for normalLink i.e playing from local storage.
         */
        intent.getStringExtra(ARG_NORMAL_LINK)?.let { filePath ->
            isLocal = true
            this.filePath = filePath
            binding.progressText.hide()
            hash = intent.getStringExtra(ARG_TORRENT_HASH)!!
            movieTitle = intent.getStringExtra(ARG_MOVIE_TITLE)!!
            movieId = intent.getIntExtra(ARG_MOVIE_ID, movieId)

            val file = File(filePath)
            startPlayer(
                file,
                file.name
            )
        } ?: Toasty.error(this, "Could not figure out the type").show()

        /** Getting subtitle local path and parsing subtitles.
         */
        intent?.getStringExtra(ARG_SUBTITLE_NAME)?.let { filename ->
            hasSubtitle = true
            subHolders = SubtitleUtils.parseSubtitles(File(SUBTITLE_LOCATION, filename).path)
        }

        if (!::subHolders.isInitialized)
            binding.subtitle.visibility = View.GONE
        else {
            /** If this has subtitles we will start a handler which will show the subtitles.
             */
            handlerHandler()
            subtitleHandler.postDelayed(updateTask, 500)
        }


        binding.giraffePlayer.findViewById<ImageView>(R.id.app_video_finish).setOnClickListener {
            if (!binding.giraffePlayer.player.onBackPressed()) {
                onBackPressed()
            }
        }
    }

    override fun onPause() {
        Log.e(TAG, "=> onPause() called")
        try {
            if (filePath == null) return super.onPause()
            sleep = true

            /** Here we are saving current player position to database,
             *  so that we can retrieve and show Play from checkbox in bottom sheet.
             */
            val position = binding.giraffePlayer.player.currentPosition
            lastPausePosition = position
            Log.e(TAG, "=> LastSavedPosition: $position")
            viewModel.updateDownload(
                hash, true, position
            )

            if (hasSubtitle) subtitleHandler.removeCallbacks(updateTask)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onPause()
    }

    override fun onResume() {
        WindowUtils.activateFullScreen(this) // For devices that has navigation button overlay
        lastSubtitlePosition = lastPausePosition
        Log.e(TAG, "onResume() called $lastPausePosition")
        try {
            if (filePath == null) return super.onResume()
            if (sleep) {
                sleep = false
                val file = File(filePath)
                startPlayer(file, file.name)
            }
        }catch (e: Exception) {}
        super.onResume()
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

        /**
         * There is an issue that whenever player is loading and user clicks on the
         * player during load. It crashes, so this is an attempt to fix it by first
         * disabling root view to accept any touch.
         *
         * This will be enabled when stream is Ready.
         */
        binding.giraffePlayer.invisible()

        torrentStream = TorrentStream.init(torrentOptions)
        torrentStream.addListener(torrentStreamListener)
        torrentStream.startStream(link)
        Toasty.info(this, getString(R.string.torrent_connect_attempt)).show()
    }

    private val torrentStreamListener = object : TorrentListener {
        override fun onStreamPrepared(torrent: Torrent) {
            Log.e(TAG, "onStreamPrepared() called with: torrent = [$torrent]")
            Toasty.info(this@TorrentPlayerActivity, getString(R.string.torrent_connect_success))
                .show()
        }

        override fun onStreamStarted(torrent: Torrent) {
            Log.e(TAG, "onStreamStarted() called with: torrent = [$torrent]")
        }

        override fun onStreamError(torrent: Torrent?, e: Exception) {
            Log.e(TAG, "onStreamError() called with: torrent = [$torrent], e = [$e]")
            Toasty.error(this@TorrentPlayerActivity, "Torrent stream error: ${e.message}")
                .show()
        }

        override fun onStreamReady(torrent: Torrent) {
            Log.e(TAG, "onStreamReady() called with: torrent = [$torrent]")

            /** Stream ready so start the player from here.
             */
            filePath = torrent.videoFile.path
            startPlayer(torrent.videoFile, torrent.videoFile.name)

            /** Showing the torrent player only when movie is ready to stream */
            binding.giraffePlayer.show()
        }

        override fun onStreamProgress(torrent: Torrent, status: StreamStatus) {
            if (lastProgress != status.progress) {
                Log.e(TAG, "onStreamProgress() => ${status.progress}")
                lastProgress = status.progress
                binding.progressText.text = "${"%.2f".format(lastProgress)}%"
            }
            if (lastProgress.toInt() >= 98) {
                binding.progressText.visibility = View.GONE
            }
        }

        override fun onStreamStopped() {
            Log.d(TAG, "onStreamStopped() called")
        }
    }

    /** A common function to load player
     */
    private fun startPlayer(file: File, title: String) {
        binding.progressBar.visibility = View.GONE

        val uri = Uri.fromFile(file)
        val videoInfo = VideoInfo(uri)
            .setTitle(title)
            .setShowTopBar(true)
            .setAspectRatio(VideoInfo.AR_ASPECT_FILL_PARENT)
            .setBgColor(colorFrom(android.R.color.black))

        binding.giraffePlayer.playerListener = listener

        binding.giraffePlayer.videoInfo(videoInfo).player.start()
    }

    /** A prefetch for subtitle Handler is done here.
     *  It is also called when there is a change in player seek.
     */
    private fun handlerHandler() {
        Log.e(TAG, "handlerHandler() called")
        binding.subtitle.text = ""
        if (lastSubtitlePosition == 0)
            models = ArrayList(subHolders)
        else {
            models.clear()

            val position = binding.giraffePlayer.player.currentPosition

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
                if (binding.giraffePlayer.isCurrentActivePlayer && !sleep) {
                    val currentPosition = binding.giraffePlayer.player!!.currentPosition

                    if (currentPosition < lastSubtitlePosition || currentPosition > lastSubtitlePosition + 7 * 1000) {
                        /** Player either seekTo backwards  || currentPosition > lastPostion + 1000*/

                        Log.e(TAG, "=> Player seekTo change")
                        subShowing = false
                        //binding.giraffePlayer.player.pause() TODO: Why we needed this I don't know but it's causing problem.
                        handlerHandler()
                     /*   if (noPlayerStartHandler) {
                            noPlayerStartHandler = false
                        } else binding.giraffePlayer.player.start()*/
                    }
                    if (models.isNotEmpty()) {
                        if (!subShowing) {
                            if (currentPosition >= models[0].startTime) {
                                binding.subtitle.text = HtmlCompat.fromHtml(models[0].text, HtmlCompat.FROM_HTML_MODE_COMPACT)
                                subShowing = true
                            }
                        } else if (subShowing) {
                            if (currentPosition >= models[0].endTime) {
                                binding.subtitle.text = ""
                                models.removeAt(0)
                                subShowing = false
                            }
                        }
                    }
                    lastSubtitlePosition = currentPosition
                }
                subtitleHandler.postDelayed(this, 100)
            } catch (e: Exception) {
                Toasty.error(
                    this@TorrentPlayerActivity,
                    "Subtitle crashed due to: ${e.message}",
                    Toasty.LENGTH_LONG
                )
                    .show()
                subtitleHandler.removeCallbacks(this)
            }
        }
    }

    override fun onBackPressed() {
        if (isLocal) {
            val bitmap = binding.giraffePlayer.player.currentDisplay.bitmap
            continueWatcherHelper.save(bitmap, binding.root.rootView as FrameLayout, getWatcher()) {
                super.onBackPressed()
            }
        } else super.onBackPressed()
    }

    override fun onDestroy() {
        try {
            if (isLocal && !continueWatcherHelper.isSaved) {
                val bitmap = binding.giraffePlayer.player.currentDisplay.bitmap
                continueWatcherHelper.saveSilent(bitmap, getWatcher())
            }
            binding.giraffePlayer.player.release()
        } catch (e: Exception) {
            Log.e(TAG, "=> Error: ${e.message}")
        }
        try {
            if (::torrentStream.isInitialized) {
                torrentStream.removeListener(torrentStreamListener)
                torrentStream.stopStream()
            }
        } catch (e: Exception) {
            Log.e(TAG, "=> Error: ${e.message}")
        }
        subtitleHandler.removeCallbacks(updateTask)
        super.onDestroy()
    }

    /** A callback to manage player. This will also seek the player to
     *  last saved position if not 0.
     */
    private var noPlayerStartHandler = false
    val listener = object : PlayerListener {
        override fun onTimedText(giraffePlayer: GiraffePlayer?, text: IjkTimedText?) {

        }

        override fun onPrepared(giraffePlayer: GiraffePlayer?) {
            Log.e(TAG, "=> onPrepared $lastPausePosition")
            giraffePlayer?.seekTo(lastPausePosition)
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
            Log.e(TAG, "=> SeekChange() ")
            if (lastPausePosition != 0 && !isLoadedfromLast) {
                giraffePlayer?.pause()
                noPlayerStartHandler = true
                if (hasSubtitle) subtitleHandler.postDelayed(updateTask, 100)
            }

            isLoadedfromLast = false

            binding.giraffePlayer.playerListener = null
        }

        override fun onInfo(giraffePlayer: GiraffePlayer?, what: Int, extra: Int): Boolean {
            Log.e(TAG, "=> Info() What = $what & Extra = $extra")
            return true
        }

        override fun onBufferingUpdate(giraffePlayer: GiraffePlayer?, percent: Int) {
        }

        override fun onCurrentStateChange(oldState: Int, newState: Int) {
        }

        override fun onLazyLoadProgress(giraffePlayer: GiraffePlayer?, progress: Int) {
        }

        override fun onError(giraffePlayer: GiraffePlayer?, what: Int, extra: Int): Boolean {
            Log.e(TAG, "=> OnError() What = $what & Extra = $extra")
            return true
        }

        override fun onPreparing(giraffePlayer: GiraffePlayer?) {
        }

        override fun onStart(giraffePlayer: GiraffePlayer?) {
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(LAST_POSITION, lastPausePosition)
        super.onSaveInstanceState(outState)
    }

    private fun getWatcher() = ContinueWatcherHelper.Watcher(movieId, movieTitle, binding.giraffePlayer.player.currentPosition)
}
