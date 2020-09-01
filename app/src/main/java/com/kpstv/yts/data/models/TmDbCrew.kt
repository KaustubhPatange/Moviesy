package com.kpstv.yts.data.models


import com.google.gson.annotations.SerializedName

data class TmDbCrew(
    @SerializedName("credit_id")
    val creditId: String,
    val department: String,
    val gender: Int,
    val id: Int,
    val job: String,
    val name: String,
    @SerializedName("profile_path")
    val profilePath: String
)