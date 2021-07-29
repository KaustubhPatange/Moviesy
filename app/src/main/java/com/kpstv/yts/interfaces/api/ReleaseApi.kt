package com.kpstv.yts.interfaces.api

import com.kpstv.yts.data.models.Release
import retrofit2.http.GET

interface ReleaseApi {
    @GET("/repos/KaustubhPatange/Moviesy/releases/latest")
    suspend fun fetchRelease(): Release

    companion object {
        const val BASE_URL = "https://api.github.com"
    }
}