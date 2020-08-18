package com.kpstv.yts.data.models

import com.google.gson.annotations.SerializedName
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.models.response.Model
import java.io.Serializable

data class MovieShort (
    val movieId: Int? = null,
    val url: String? = null,
    val title: String,
    val year: Int?,
    val rating: Double,
    val runtime: Int,
    val imdbCode: String? = null,
    @SerializedName("medium_cover_image")
    val bannerUrl: String
): Serializable {
    companion object {
        fun from(data: TmDbMovie) = MovieShort(
            movieId = data.id.toInt(),
            title = data.title,
            rating = data.rating,
            year = data.release_date?.split("-")?.get(0)?.toInt() ?: 0,
            bannerUrl = "${AppInterface.TMDB_IMAGE_PREFIX}${data.bannerPath}",
            runtime = data.runtime
        )

        fun from(data: Model.response_cast_movie.Cast): MovieShort {
            return MovieShort(
                movieId = data.id,
                title = data.originalTitle,
                bannerUrl = "${AppInterface.TMDB_IMAGE_PREFIX}${data.posterPath}",
                runtime = 0,
                rating = data.voteAverage,
                year = data.releaseDate.split("-")[0].toInt()
            )
        }
    }
}