package com.kpstv.yts.interfaces.listener

interface FavouriteListener {
    fun onToggleFavourite(id: Int?)
    fun isMovieFavourite(value: Boolean)
}