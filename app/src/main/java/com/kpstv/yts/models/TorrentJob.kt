package com.kpstv.yts.models

import java.io.Serializable

data class TorrentJob (
    val title: String,
    val bannerUrl: String,
    val progress: Int,
    val seeds: Int,
    val downloadSpeed: Float,
    val currentSize: Long,
    val totalSize: Long?,
    val isPlay: Boolean,
    val status: String,
    val peers: Int,
    val magnetHash: String
    ) : Serializable