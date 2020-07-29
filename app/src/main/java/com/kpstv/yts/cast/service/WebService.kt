package com.kpstv.yts.cast.service

import android.app.IntentService
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.kpstv.yts.cast.CastHelper
import io.github.dkbai.tinyhttpd.nanohttpd.webserver.SimpleWebServer

class WebService : IntentService("blank") {
    private val TAG = javaClass.simpleName

    override fun onStart(intent: Intent?, startId: Int) {
        SimpleWebServer.stopServer()
        super.onStart(intent, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        try {
            /** Running a server on Internal storage.
             *
             * I know the method [Environment.getExternalStorageDirectory] is deprecated
             * but it is needed to start the server in the required path.
             */

            SimpleWebServer.runServer(
                arrayOf(
                    "-h",
                    CastHelper.deviceIpAddress,
                    "-p",
                    CastHelper.PORT,
                    "-d",
                    Environment.getExternalStorageDirectory().absolutePath
                )
            )
            Log.d(TAG, "Service Started on ${CastHelper.deviceIpAddress}:${CastHelper.PORT}")
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        SimpleWebServer.stopServer()
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }
}