package com.kpstv.yts.data.models

import com.google.gson.annotations.SerializedName
import com.kpstv.yts.data.models.response.Model
import java.io.Serializable

data class MovieShort(
    val movieId: Int? = null,
    val url: String? = null,
    val title: String,
    val year: Int?,
    val rating: Double,
    val runtime: Int,
    val imdbCode: String? = null,
    @SerializedName("medium_cover_image")
    val bannerUrl: String
) : Serializable {
    companion object {
        fun from(data: TmDbMovie) = MovieShort(
            movieId = data.movieId,
            title = data.title,
            rating = data.rating,
            year = safeYear(data.release_date),
            bannerUrl = data.getPosterImage(),
            runtime = data.runtime
        )

        fun from(data: Model.response_cast_movie.Cast)= MovieShort(
            movieId = data.id,
            title = data.originalTitle,
            bannerUrl = data.getPosterImage(),
            runtime = 0,
            rating = data.voteAverage,
            year = safeYear(data.releaseDate)
        )

        fun from(data: Movie) = MovieShort(
            movieId = data.id,
            imdbCode = data.imdb_code,
            title = data.title,
            year = data.year,
            rating = data.rating,
            runtime = data.runtime,
            bannerUrl = data.medium_cover_image,
            url = data.url
        )

        private fun safeYear(releaseData: String?): Int {
            val year = releaseData?.split("-")?.get(0)
            return if (year.isNullOrEmpty())
                0
            else year.toInt()
        }
    }
}