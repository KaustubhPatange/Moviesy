package com.kpstv.yts.interfaces.api

import com.kpstv.yts.data.models.response.Model
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMdbApi {

    /** Get similar movie by either passing TMDB ID or IMDB ID */

    @GET("movie/{id}/similar")
    fun getSimilars(@Path("id") id: String,@Query("page") page: Int=1): Deferred<Model.response_tmdb_movies>

    /** Get recommended movie by passing TMDB ID */

    @GET("movie/{id}/recommendations")
    fun getRecommendations(@Path("id") id: Int,@Query("page") page: Int=1): Deferred<Model.response_tmdb_movies>

    /** Get movie details from TMDB Movie ID or IMDB ID */

    @GET("movie/{id}")
    fun getMovie(@Path("id") id: String): Deferred<Model.response_tmdb_movie>

    /** Get cast details from TMDB Movie ID or IMDB ID */

    @GET("movie/{id}/credits")
    fun getCast(@Path("id") id: String): Deferred<Model.response_tmdb_cast>

    /*companion object {
        private var tmdbApi: TMdbPlaceholderApi? = null
        operator fun invoke(retrofitUtils: RetrofitUtils): TMdbPlaceholderApi {
            val requestInterceptor = Interceptor { chain ->
                val url = chain.request()
                    .url
                    .newBuilder()
                    .addQueryParameter("api_key", TMDB_API_KEY)
                    .build()
                val request = chain.request()
                    .newBuilder()
                    .url(url)
                    .build()
                return@Interceptor chain.proceed(request)
            }
            return tmdbApi
                ?: retrofitUtils.getRetrofitBuilder()
                    .baseUrl(TMDB_BASE_URL)
                    .client(retrofitUtils.getHttpBuilder().addInterceptor(requestInterceptor).build())
                    .build()
                    .create(TMdbPlaceholderApi::class.java)
                    .also { tmdbApi = it }
        }
    }*/
}