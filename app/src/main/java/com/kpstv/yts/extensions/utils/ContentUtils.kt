package com.kpstv.yts.extensions.utils

import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import java.net.URLDecoder

/**
 * The class is used to work with content uris and files.
 *
 * I could've used scoped storage, but the torrent downloader this app
 * used is old and it only supports file path instead of uri. Also I can't
 * update the torrent downloader typically because I've a limited knowledge
 * on how client works and it's also a time consuming work.
 */
object ContentUtils {
    private val TAG = javaClass.simpleName

    /**
     * Returns file path from uri.
     *
     * Currently supports only directory path created from [Intent.ACTION_OPEN_DOCUMENT_TREE]
     */
    fun getFileString(uri: Uri): String? {
        if (uri.scheme != "content")
            return null
        var path = URLDecoder.decode(uri.path, "UTF-8")
            .replace("/tree/", "")
        if (!path.startsWith("primary"))  // External directories are not supported
            return null
        path = path.replace("primary:","")
        when (uri.authority) {
            "com.android.externalstorage.documents" -> {
                return Environment.getExternalStoragePublicDirectory(path)?.path
            }
        }
        return null
    }

    /**
     * Return file from uri.
     */
    fun getFile(uri: Uri): File? {
        getFileString(uri)?.let { path ->
            return File(path)
        }
        return null
    }
}