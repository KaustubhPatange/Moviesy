package com.kpstv.yts.interfaces.listener

import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Crew
import com.kpstv.yts.data.models.Movie

interface MovieListener {
    fun onStarted()
    fun onComplete(movie: Movie)
    fun onCastFetched(casts: List<Cast>, crews: List<Crew>)
    fun onFailure(e: Exception)
}