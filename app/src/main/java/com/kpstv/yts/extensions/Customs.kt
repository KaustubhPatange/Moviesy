package com.kpstv.yts.extensions

import android.view.View
import androidx.annotation.DrawableRes
import com.kpstv.yts.YTSQuery
import com.kpstv.yts.fragments.GenreFragment
import java.io.IOException

class NoInternetException(message: String) : IOException(message)

fun ArrayList<GenreFragment.LocalGenreModel>.add(title: String, @DrawableRes drawable: Int, genre: YTSQuery.Genre) {
    this.add(GenreFragment.LocalGenreModel(title, drawable, genre))
}

fun View.hide() {
    visibility = View.GONE
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