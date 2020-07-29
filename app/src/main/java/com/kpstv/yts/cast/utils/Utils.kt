package com.kpstv.yts.cast.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Environment
import android.util.Log
import com.kpstv.yts.cast.CastHelper
import java.io.File
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Utils {
    companion object {
        fun findIPAddress(context: Context): String? {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            try {
                return if (wifiManager.connectionInfo != null) {
                    val wifiInfo = wifiManager.connectionInfo
                    InetAddress.getByAddress(
                        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(wifiInfo.ipAddress)
                            .array()
                    ).hostAddress
                } else
                    null
            } catch (e: Exception) {
                Log.e(Utils::class.java.name, "Error finding IpAddress: ${e.message}", e)
            }
            return null
        }

        fun getRemoteFileName(deviceIp: String?, file: File): String? {
            if (deviceIp == null) return null
            val root = Environment.getExternalStorageDirectory().absolutePath
            return "http://${deviceIp}:${CastHelper.PORT}${file.absolutePath.replace(root, "")}"
        }
    }
}