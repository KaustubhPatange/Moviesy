package com.kpstv.yts.data.models

import com.google.gson.annotations.SerializedName

data class TmDbMovie (
    val uid: Int,
    val id: String,
    @SerializedName("vote_count")
    val likes: Int,
    @SerializedName("vote_average")
    val rating: Double,
    val title: String,
    val release_date: String?,
    val runtime: Int,
    @SerializedName("original_language")
    val language: String,
    val original_title: String,
    @SerializedName("backdrop_path")
    val backgroundPath: String,
    @SerializedName("poster_path")
    val bannerPath: String
)