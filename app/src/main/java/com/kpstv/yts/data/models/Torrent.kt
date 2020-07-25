package com.kpstv.yts.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Torrent(
    var title: String, var banner_url: String,
    val url: String, val hash: String, val quality: String,
    val type: String, val seeds: Int, val peers: Int,
    @SerializedName("size") val size_pretty: String,
    @SerializedName("size_bytes") val size: Long,
    val date_uploaded: String, val date_uploaded_unix: String,
    var movieId: Int, var imdbCode: String
) : Serializable