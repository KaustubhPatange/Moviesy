package com.kpstv.yts.utils

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kpstv.yts.utils.interceptors.NetworkConnectionInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface NetworkUtils {
    companion object {

        private var retrofitBuilder: Retrofit.Builder? = null
        private var httpBuilder: OkHttpClient.Builder? = null

        private lateinit var interceptor: NetworkConnectionInterceptor

        operator fun invoke(interceptor: NetworkConnectionInterceptor): Companion {
            this.interceptor = interceptor
            return this
        }

        fun getRetrofitBuilder(): Retrofit.Builder {
            return retrofitBuilder ?: Retrofit.Builder().apply {
                addCallAdapterFactory(CoroutineCallAdapterFactory())
                addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder().serializeNulls().create()
                    )
                )
                client(getHttpClient())
            }.also { retrofitBuilder = it }
        }

        fun getHttpBuilder(): OkHttpClient.Builder {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            return httpBuilder
                ?: OkHttpClient.Builder()
                    // .addInterceptor(loggingInterceptor)  // TODO: Add this interceptor when debugging
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .also { httpBuilder = it }
        }

        fun getHttpClient() = getHttpBuilder().build()
    }
}