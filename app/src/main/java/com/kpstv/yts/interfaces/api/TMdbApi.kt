package com.kpstv.yts.interfaces.api

import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.response.Model
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMdbApi {

    /** Get similar movie by either passing TMDB ID or IMDB ID */

    @GET("movie/{id}/similar")
    suspend fun getSimilar(@Path("id") id: String, @Query("page") page: Int=1): Model.response_tmdb_movies

    /** Get recommended movie by passing TMDB ID */

    @GET("movie/{id}/recommendations")
    suspend fun getRecommendations(@Path("id") id: Int, @Query("page") page: Int=1): Model.response_tmdb_movies

    /** Get movie details from TMDB Movie ID or IMDB ID */

    @GET("movie/{id}")
    suspend fun getMovie(@Path("id") id: String): TmDbMovie

    /** Get cast details from TMDB Movie ID or IMDB ID */

    @GET("movie/{id}/credits")
    suspend fun getCast(@Path("id") id: String): Model.response_tmdb_cast

    @GET("search/movie")
    suspend fun getSearch(@Query("query") query: String, @Query("page") page: Int=1): Model.response_tmdb_movies

    @GET("person/{person_id}/movie_credits")
    suspend fun getPersonCredits(@Path("person_id") id: Int): Model.response_cast_movie
}