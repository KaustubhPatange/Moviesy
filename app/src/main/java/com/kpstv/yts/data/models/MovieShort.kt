package com.kpstv.yts.data.models

import com.google.gson.annotations.SerializedName
import com.kpstv.yts.AppInterface
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
            movieId = data.id.toInt(),
            title = data.title,
            rating = data.rating,
            year = safeYear(data.release_date),
            bannerUrl = "${AppInterface.TMDB_IMAGE_PREFIX}${data.bannerPath}",
            runtime = data.runtime
        )

        fun from(data: Model.response_cast_movie.Cast)= MovieShort(
            movieId = data.id,
            title = data.originalTitle,
            bannerUrl = "${AppInterface.TMDB_IMAGE_PREFIX}${data.posterPath}",
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