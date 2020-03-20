package com.kpstv.yts.interfaces.listener

import com.kpstv.yts.models.Cast
import com.kpstv.yts.models.Movie
import java.lang.Exception

interface MovieListener {
    fun onStarted()
    fun onComplete(movie: Movie)
    fun onCastFetched(casts: ArrayList<Cast>)
    fun onFailure(e: Exception)
}