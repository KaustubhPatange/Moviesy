package com.kpstv.yts.data.models

import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.ConverterType
import java.io.Serializable

@AutoGenerateConverter(using = ConverterType.GSON)
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
    ) : Serializable {
    companion object {
        fun from(model: Torrent, status: String = "Paused") =
            TorrentJob(
                title = model.title,
                bannerUrl = model.banner_url,
                progress = 0,
                seeds = model.seeds,
                downloadSpeed = 0f,
                currentSize = 0,
                totalSize = model.size,
                isPlay = false,
                status = status,
                peers = model.peers,
                magnetHash = model.hash
            )
    }
}