package com.kpstv.yts.extensions

import android.view.View
import androidx.annotation.DrawableRes
import com.kpstv.yts.YTSQuery
import com.kpstv.yts.fragments.GenreFragment
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class NoInternetException(message: String) : IOException(message)

fun<T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>>{
    return lazy {
        GlobalScope.async(start = CoroutineStart.LAZY) {
            block.invoke(this)
        }
    }
}

fun ArrayList<GenreFragment.LocalGenreModel>.add(title: String, @DrawableRes drawable: Int, genre: YTSQuery.Genre) {
    this.add(GenreFragment.LocalGenreModel(title, drawable, genre))
}

fun View.hide() {
    visibility = View.GONE
}

fun File.deleteRecursive() {
    if (this.isDirectory)
        for (child in this.listFiles()) {
            child.deleteRecursive()
        }
    this.delete();
}

fun View.show() {
    visibility = View.VISIBLE
}

enum class MovieBase {
    YTS,
    TMDB
}

enum class MovieType {
    Suggestion,
    Recommend
}