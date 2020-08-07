package com.kpstv.yts.interfaces.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface AppApi {
    @GET
    @Streaming
    suspend fun fetchFileAsync(@Url url: String): ResponseBody
}