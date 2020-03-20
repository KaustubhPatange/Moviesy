package com.kpstv.yts.models

import java.io.Serializable

data class Cast(
    val name: String, val character_name: String, val url_small_image: String,
    val imdb_code: String
) : Serializable