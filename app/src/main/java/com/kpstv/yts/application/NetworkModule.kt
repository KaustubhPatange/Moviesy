package com.kpstv.yts.application

import android.content.Context
import androidx.room.Room
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.db.localized.RecommendDatabase
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.interfaces.api.TMdbPlaceholderApi
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideYTSApi(
        retrofitUtils: RetrofitUtils
    ): YTSPlaceholderApi {
        return retrofitUtils.getRetrofitBuilder()
                .baseUrl(AppInterface.YTS_BASE_API_URL)
                .build()
                .create(YTSPlaceholderApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTMDbApi(
        retrofitUtils: RetrofitUtils
    ): TMdbPlaceholderApi {
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
                .create(TMdbPlaceholderApi::class.java)
    }
}