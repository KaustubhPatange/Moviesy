package com.kpstv.yts.extensions

import okhttp3.ResponseBody
import java.io.*

data class Progress (
    val currentBytes: Long,
    val totalBytes: Long
)

class ProgressStreamer (
    private val onProgressChange: (Progress) -> Unit,
    private val onComplete: () -> Unit
) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    fun write(body: ResponseBody, file: File) {
        try {
            val fileReader = ByteArray(4096)
            val fileSize = body.contentLength()
            var currentSize: Long = 0
            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)
            while (true) {
                val read: Int = inputStream?.read(fileReader) ?: 0
                if (read == -1) {
                    break
                }
                outputStream?.write(fileReader, 0, read)
                currentSize += read.toLong()

                onProgressChange.invoke(Progress(currentSize, fileSize))
                if (currentSize == fileSize)
                    onComplete.invoke()
            }
            outputStream?.flush()
        } catch (e: IOException) {
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    fun stop() {
        try {
            inputStream?.close()
            outputStream?.flush()
            outputStream?.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}
