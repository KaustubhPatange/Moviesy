package com.kpstv.yts.interfaces.listener

import com.kpstv.yts.data.models.MovieShort

interface MoviesListener {
    fun onStarted()
    fun onFailure(e: Exception)
    fun onComplete(movies: ArrayList<MovieShort>, queryMap: Map<String, String>, isMoreAvailable: Boolean)
}