package com.kpstv.yts.di

import com.kpstv.yts.AppInterface
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.interfaces.api.AppApi
import com.kpstv.yts.interfaces.api.ReleaseApi
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.interfaces.api.YTSApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideReleaseApi(
        retrofitUtils: RetrofitUtils
    ): ReleaseApi {
        return retrofitUtils.getRetrofitBuilder()
            .baseUrl(ReleaseApi.BASE_URL)
            .build()
            .create(ReleaseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppApi(
        retrofitUtils: RetrofitUtils
    ): AppApi {
        return retrofitUtils.getRetrofitBuilder()
            .baseUrl(AppInterface.YTS_BASE_API_URL) // baseUrl currently does not matter
            .build()
            .create(AppApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYTSApi(
        retrofitUtils: RetrofitUtils
    ): YTSApi {
        return retrofitUtils.getRetrofitBuilder()
            .baseUrl(AppInterface.YTS_BASE_API_URL)
            .build()
            .create(YTSApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTMDbApi(
        retrofitUtils: RetrofitUtils
    ): TMdbApi {
        val requestInterceptor = Interceptor { chain ->
            val url = chain.request()
                .url
                .newBuilder()
                .addQueryParameter("api_key", AppInterface.TMDB_API_KEY)
                .build()
            val request = chain.request()
                .newBuilder()
                .url(url)
                .build()
            return@Interceptor chain.proceed(request)
        }
        return  retrofitUtils.getRetrofitBuilder()
                .baseUrl(AppInterface.TMDB_BASE_URL)
                .client(retrofitUtils.getHttpBuilder().addInterceptor(requestInterceptor).build())
                .build()
                .create(TMdbApi::class.java)
    }
}