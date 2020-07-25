package com.kpstv.yts.extensions.utils

import com.kpstv.yts.extensions.Coroutines
import kotlinx.coroutines.delay
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProxyUtils @Inject constructor(
    private val retrofitUtils: RetrofitUtils
) {
    /**
     * Method will check for updated proxy which are available on app
     * database.
     */
    suspend fun check() = Coroutines.io {
        retrofitUtils.getHttpClient()
            .newCall(Request.Builder().build())
    }
}