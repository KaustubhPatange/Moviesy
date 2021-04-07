package com.kpstv.yts.ui.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.extensions.toFile
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.dialogs.ProgressDialog

@Deprecated("Use MainCastHelper2")
class MainCastHelper(private val mainActivity: MainActivity, private val castHelper: CastHelper) {

    fun setUpCastRelatedStuff() = with(mainActivity) {
        val filters = IntentFilter()
        filters.addAction(BROADCAST_STREAM_START)
        filters.addAction(BROADCAST_STREAM_READY)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, filters)

        mainActivity.lifecycle.addObserver(castObserver)
    }

    fun unregister() = with(mainActivity) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private val castObserver = object: DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            unregister()
            super.onDestroy(owner)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                BROADCAST_STREAM_START -> {

                }
                BROADCAST_STREAM_READY -> {
                    val mediaFile = intent.getStringExtra(ARG_MEDIA_FILE_PATH).toFile() ?: return
                    val bannerFile = intent.getStringExtra(ARG_BANNER_FILE_PATH).toFile()
                    val srtFile = intent.getStringExtra(ARG_SRT_FILE_PATH).toFile()
                    castHelper.loadMedia(
                        mediaFile = mediaFile,
                        bannerFile = bannerFile,
                        srtFile = srtFile
                    )
                }
            }
        }
    }

    companion object {
        const val ARG_MEDIA_FILE_PATH = "arg_media_file_path"
        const val ARG_BANNER_FILE_PATH = "arg_banner_file_path"
        const val ARG_SRT_FILE_PATH = "arg_srt_file_path"

        const val BROADCAST_STREAM_START = "broadcast_stream_start"
        const val BROADCAST_STREAM_READY = "broadcast_stream_ready"
    }
}

class MainCastHelper2(private val context: Context, private val lifecycle: Lifecycle, private val castHelper: CastHelper) {

    fun setUpCastRelatedStuff() = with(context) {
        val filters = IntentFilter()
        filters.addAction(BROADCAST_STREAM_START)
        filters.addAction(BROADCAST_STREAM_READY)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, filters)

        lifecycle.addObserver(castObserver)
    }

    fun unregister() = with(context) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private val castObserver = object: DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            unregister()
            super.onDestroy(owner)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                BROADCAST_STREAM_START -> {

                }
                BROADCAST_STREAM_READY -> {
                    val mediaFile = intent.getStringExtra(ARG_MEDIA_FILE_PATH).toFile() ?: return
                    val bannerFile = intent.getStringExtra(ARG_BANNER_FILE_PATH).toFile()
                    val srtFile = intent.getStringExtra(ARG_SRT_FILE_PATH).toFile()
                    castHelper.loadMedia(
                        mediaFile = mediaFile,
                        bannerFile = bannerFile,
                        srtFile = srtFile
                    )
                }
            }
        }
    }

    companion object {
        const val ARG_MEDIA_FILE_PATH = "arg_media_file_path"
        const val ARG_BANNER_FILE_PATH = "arg_banner_file_path"
        const val ARG_SRT_FILE_PATH = "arg_srt_file_path"

        const val BROADCAST_STREAM_START = "broadcast_stream_start"
        const val BROADCAST_STREAM_READY = "broadcast_stream_ready"
    }
}