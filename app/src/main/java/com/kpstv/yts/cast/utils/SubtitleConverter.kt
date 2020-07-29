package com.kpstv.yts.cast.utils

import java.io.File

object SubtitleConverter {
    private const val LINE_NUMBER_PATTERN = "^(\\d+)$"
    private const val TIMESTAMP_PATTERN = "([\\d:,]+)\\s-->\\s([\\d:,]+)"

    /**
     * Converts srt text data to vtt text data.
     *
     * @param data Complete file text from srt file.
     */
    fun convertFromSrtToVtt(data: String): String? {
        if (data.isEmpty()) return null
        val sb = StringBuilder()
        sb.append("WEBVTT").append("\n").append("\n")
        data.lines().drop(1).forEach { line ->
            val trimmedLine = line.trim()
            when {
                LINE_NUMBER_PATTERN.toRegex().containsMatchIn(trimmedLine) -> sb.append("\n")
                TIMESTAMP_PATTERN.toRegex()
                    .containsMatchIn(trimmedLine) -> sb.append(trimmedLine.replace(",", "."))
                    .append("\n")
                trimmedLine.isNotEmpty() -> sb.append(trimmedLine).append("\n")
            }
        }
        return sb.toString()
    }

    /**
     * Converts srtFile to VttFile. Could potentially block the calling thread.
     *
     * If [outVttFile] is not specified the file will be created in the same
     * directory.
     *
     * @param outVttFile Optional parameter to specify the output vtt file.
     *
     */
    fun convertFromSrtToVtt(srtFile: File, outVttFile: File? = null): File? {
        val vttFile =
            outVttFile ?: File(srtFile.parentFile, "${srtFile.nameWithoutExtension}.vtt")
        val allText = srtFile.readText()
        convertFromSrtToVtt(allText)?.let {
            vttFile.writeText(it)
            return vttFile
        }
        return null
    }
}