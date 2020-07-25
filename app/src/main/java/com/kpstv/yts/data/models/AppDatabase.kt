package com.kpstv.yts.data.models


import com.google.gson.annotations.SerializedName

data class AppDatabase(
    val misc: Misc,
    val tmdb: Tmdb,
    val yts: Yts
) {
    data class Misc(
        val suggestionApi: String
    )

    data class Tmdb(
        val apiKey: String,
        val base: String,
        val image: String
    )

    data class Yts(
        val base: String,
        val yify: String
    )
}