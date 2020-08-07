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
    fun write(body: ResponseBody, file: File) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val fileReader = ByteArray(4096)
            val fileSize = body.contentLength()
            var currentSize: Long = 0
            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)
            while (true) {
                val read: Int = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                currentSize += read.toLong()

                onProgressChange.invoke(Progress(currentSize, fileSize))
                if (currentSize == fileSize)
                    onComplete.invoke()
            }
            outputStream.flush()
        } catch (e: IOException) {
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}
