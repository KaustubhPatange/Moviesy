package com.kpstv.yts.interfaces.api

import retrofit2.http.GET

interface AppApi {
    @GET
    fun getDatabaseAsync()
}