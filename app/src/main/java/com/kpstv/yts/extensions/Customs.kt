package com.kpstv.yts.extensions

import android.app.Activity
import android.content.Intent
import java.io.File
import java.util.*

fun String.small() = toLowerCase(Locale.ROOT)

fun Activity.startActivityAndFinish(intent: Intent) {
    startActivity(intent)
    finish()
}

fun File.deleteRecursive() {
    if (this.isDirectory)
        for (child in this.listFiles()) {
            child.deleteRecursive()
        }
    this.delete();
}

fun String?.toFile(): File? {
    return if (this != null) File(this) else null
}

enum class MovieBase {
    YTS,
    TMDB
}

enum class MovieType {
    Suggestion,
    Recommend
}

enum class SearchType {
    Google,
    TMDB
}