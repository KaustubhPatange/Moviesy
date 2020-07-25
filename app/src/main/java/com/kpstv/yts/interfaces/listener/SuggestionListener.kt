package com.kpstv.yts.interfaces.listener

import com.kpstv.yts.data.models.TmDbMovie

interface SuggestionListener {
    fun onStarted()
    fun onComplete(movies: ArrayList<TmDbMovie>, tag: String? = null, isMoreAvailable: Boolean=true)
    fun onFailure(e: Exception)
}