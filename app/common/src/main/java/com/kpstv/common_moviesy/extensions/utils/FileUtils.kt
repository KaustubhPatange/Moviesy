package com.kpstv.common_moviesy.extensions.utils

import java.io.File

object FileUtils {
    @JvmName("deleteRecurse")
    fun deleteRecursive(fileOrDirectory: File?) {
        if (fileOrDirectory == null) return
        val allFiles = fileOrDirectory.listFiles()
        if (fileOrDirectory.isDirectory && allFiles != null)
            for (child in allFiles) {
                deleteRecursive(child)
            }
        fileOrDirectory.delete()
    }
}