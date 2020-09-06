package com.kpstv.yts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.ACTION_UPDATE
import com.kpstv.yts.AppInterface.Companion.STOP_SERVICE
import com.kpstv.yts.AppInterface.Companion.TORRENT_NOT_SUPPORTED
import com.kpstv.yts.AppInterface.Companion.UNPAUSE_JOB
import com.kpstv.yts.R
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.extensions.utils.UpdateUtils
import com.kpstv.yts.services.CastTorrentService
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.services.UpdateWorker
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommonBroadCast : BroadcastReceiver() {

    companion object {
        const val STOP_UPDATE_WORKER = "com.kpstv.actions.stop_update_worker"
        const val STOP_CAST_SERVICE = "com.kpstv.actions.stop_cast_service"
    }

    @Inject lateinit var updateUtils: UpdateUtils

    private val TAG = "CommonBroadCast"

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            STOP_SERVICE -> {
                val serviceIntent = Intent(context, DownloadService::class.java)
                context?.stopService(serviceIntent)
            }
            TORRENT_NOT_SUPPORTED -> {
                AlertNoIconDialog.Companion.Builder(context!!).apply {
                    setTitle(context.getString(R.string.timeout_error_title))
                    setMessage(context.getString(R.string.timeout_error_text))
                    setPositiveButton(context.getString(R.string.alright), null)
                }.show()
            }
            UNPAUSE_JOB -> {

                Log.e(TAG, "=> UNPAUSE JOB")

                val serviceDownload = Intent(context, DownloadService::class.java)
                serviceDownload.putExtra(DownloadService.TORRENT_JOB, intent.getSerializableExtra("model") as Torrent)
                ContextCompat.startForegroundService(context!!, serviceDownload)
            }
            STOP_UPDATE_WORKER -> {
                Log.e(TAG, "Update Stopped")
                UpdateWorker.stop(context ?: return)
            }
            STOP_CAST_SERVICE -> {
                val serviceIntent = Intent(context, CastTorrentService::class.java)
                context?.stopService(serviceIntent)
            }
        }
    }
}