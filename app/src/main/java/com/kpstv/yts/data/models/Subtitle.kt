package com.kpstv.yts.data.models

import java.io.Serializable

data class Subtitle(
    val country: String,
    val text: String,
    val likes: Int,
    val uploader: String,
    val fetchEndpoint: String,
    var isDownload: Boolean = false
) : Serializable