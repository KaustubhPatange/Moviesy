package com.kpstv.yts.ui.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.yts.R
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.extensions.toFile
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.dialogs.ProgressDialog

class MainCastHelper(private val mainActivity: MainActivity, private val castHelper: CastHelper) {

    private var progressDialog: ProgressDialog? = null

    fun setUpCastRelatedStuff() = with(mainActivity) {
        val filters = IntentFilter()
        filters.addAction(BROADCAST_STREAM_START)
        filters.addAction(BROADCAST_STREAM_READY)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, filters)
    }

    fun unregister() = with(mainActivity) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                BROADCAST_STREAM_START -> {
                    /** Show a progress dialog */
                    progressDialog = ProgressDialog.Builder(mainActivity)
                        .setTitle(mainActivity.getString(R.string.casting))
                        .setMessage(mainActivity.getString(R.string.casting_waiting))
                        .setCancelable(false)
                        .setOnCloseListener {
                            // Do something on close
                        }
                        .build()
                    progressDialog?.show()
                }
                BROADCAST_STREAM_READY -> {
                    /** Dismiss dialog when stream about to be started. */

                    progressDialog?.dismiss()

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