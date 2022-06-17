package com.kpstv.yts.data.models

import android.net.Uri
import com.kpstv.yts.ui.helpers.SubtitleHelper.Companion.removeSpecialCharacters
import java.io.Serializable
import java.security.MessageDigest

data class Subtitle(
    val country: String,
    val text: String,
    val likes: Int,
    val uploader: String,
    val fetchEndpoint: String,
    var isDownload: Boolean = false
) : Serializable {
    fun getDownloadFileName(): String {
        val uri = Uri.parse(fetchEndpoint)
        return "${uri.lastPathSegment}-${getMD5Text()}.srt"
    }
    fun getMD5Text(): String {
        return MessageDigest.getInstance("MD5").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}