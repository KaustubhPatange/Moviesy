package com.kpstv.yts.data.models

import com.google.gson.annotations.SerializedName
import com.kpstv.yts.AppInterface

data class TmDbMovie(
    @SerializedName("id")
    val movieId: Int,
    @SerializedName("imdb_id")
    val imdbCode: String,
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
    val bannerSuffix: String,
    @SerializedName("poster_path")
    val posterSuffix: String
) {
    fun getBannerImage(): String = AppInterface.TMDB_IMAGE_PREFIX + bannerSuffix
    fun getPosterImage(): String = AppInterface.TMDB_IMAGE_PREFIX + posterSuffix
}