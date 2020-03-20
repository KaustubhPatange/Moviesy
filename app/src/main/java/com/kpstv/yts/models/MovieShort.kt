package com.kpstv.yts.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MovieShort (
    val movieId: Int? = null,
    val url: String? = null,
    val title: String,
    val year: Int?,
    val rating: Double,
    val runtime: Int,
    val imdbCode: String? = null,
    @SerializedName("medium_cover_image")
    val bannerUrl: String
): Serializable