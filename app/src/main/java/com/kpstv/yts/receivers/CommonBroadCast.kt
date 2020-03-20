package com.kpstv.yts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.kpstv.yts.AppInterface.Companion.STOP_SERVICE
import com.kpstv.yts.AppInterface.Companion.TORRENT_NOT_SUPPORTED
import com.kpstv.yts.R
import com.kpstv.yts.dialogs.AlertNoIconDialog
import com.kpstv.yts.services.DownloadService

class CommonBroadCast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            STOP_SERVICE -> {
                val serviceIntent = Intent(context, DownloadService::class.java)
                context!!.stopService(serviceIntent)
            }
            TORRENT_NOT_SUPPORTED -> {
                AlertNoIconDialog.Companion.Builder(context!!).apply {
                    setTitle(context.getString(R.string.timeout_error_title))
                    setMessage(context.getString(R.string.timeout_error_text))
                    setPositiveButton(context.getString(R.string.alright),null)
                }.show()
            }
        }
    }
}