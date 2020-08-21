package com.kpstv.yts.extensions

import android.app.Activity
import android.content.Intent
import androidx.annotation.DrawableRes
import com.kpstv.yts.ui.fragments.GenreFragment
import kotlinx.coroutines.*
import java.io.File
import java.util.*

fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>> {
    return lazy {
        GlobalScope.async(start = CoroutineStart.LAZY) {
            block.invoke(this)
        }
    }
}

fun String.small() = toLowerCase(Locale.ROOT)

fun ArrayList<GenreFragment.LocalGenreModel>.add(
    title: String,
    @DrawableRes drawable: Int,
    genre: YTSQuery.Genre
) {
    this.add(GenreFragment.LocalGenreModel(title, drawable, genre))
}

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