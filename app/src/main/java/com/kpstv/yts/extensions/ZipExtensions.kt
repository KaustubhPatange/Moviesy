package com.kpstv.yts.extensions

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

data class ZipIO (val entry: ZipEntry, val output: File)

fun File.unzip(targetDirectory: File? = null) {

    val rootFolder = targetDirectory ?: File(parentFile?.absolutePath + File.separator + nameWithoutExtension)
    if (!rootFolder.exists()) {
        rootFolder.mkdirs()
    }

    ZipFile(this).use { zip ->
        zip.entries()
            .asSequence()
            .map {
                val outputFile = File(rootFolder.absolutePath + File.separator + it.name)
                ZipIO(it, outputFile)
            }
            .map {
                it.output.parentFile?.run{
                    if (!exists()) mkdirs()
                }
                it
            }
            .filter { !it.entry.isDirectory }
            .forEach { (entry, output) ->
                zip.getInputStream(entry).use { input ->
                    output.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
    }
}