package com.kpstv.yts.models

import java.io.Serializable

data class TorrentJob (
    val title: String,
    val bannerUrl: String,
    val progress: Int,
    val seeds: Int,
    val downloadSpeed: Float,
    val currentSize: Long,
    val totalSize: Long? = null,
    val isPlay: Boolean,
    var status: String,
    val peers: Int,
    val magnetHash: String
    ) : Serializable