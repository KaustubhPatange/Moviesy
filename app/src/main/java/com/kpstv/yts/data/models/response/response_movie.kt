package com.kpstv.yts.data.models.response

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.data.models.*
import com.kpstv.yts.data.models.data.data_movie
import java.io.Serializable

object Model {
    data class response_movie(val status: String, val status_message: String, val data: data_movie)
    data class response_tmdb_movies(
        val page: Int,
        val total_pages: Int,
        val total_results: Int,
        val results: ArrayList<TmDbMovie>
    )

    data class response_tmdb_movie(
        val id: Int,
        val title: String,
        val imdb_id: String
    )

    data class response_tmdb_cast(
        val id: Int,
        val cast: ArrayList<TmDbCast>?
    )

    @Entity(tableName = "table_favourites")
    data class response_favourite(
        @PrimaryKey(autoGenerate = true)
        val id: Int? = null,
        val movieId: Int,
        val imdbCode: String,
        val title: String,
        val imageUrl: String,
        val rating: Double,
        val runtime: Int,
        val year: Int
    ) {
        companion object {
            fun from(movie: MovieShort) =
                response_favourite(
                    movieId = movie.movieId!!,
                    year = movie.year!!,
                    rating = movie.rating,
                    runtime = movie.runtime,
                    imageUrl = movie.bannerUrl,
                    title = movie.title,
                    imdbCode = movie.imdbCode!!
                )
        }
    }

    @Entity(tableName = "table_pause")
    data class response_pause(
        @PrimaryKey(autoGenerate = true)
        val id: Int? = null,
        val job: TorrentJob,
        val hash: String,
        val torrent: Torrent?,
        val saveLocation: String?
    ) : Serializable

    @Entity(tableName = "table_download")
    data class response_download(
        @PrimaryKey(autoGenerate = true)
        val id: Int? = null,
        val movieId: Int?,
        val imdbCode: String?,
        val title: String,
        val imagePath: String?,
        val downloadPath: String?,
        val size: Long,
        val date_downloaded: String?,
        val total_video_length: Long,
        val hash: String,
        val videoPath: String?,
        @ColumnInfo(name = "recentlyPlayed") val recentlyPlayed: Boolean = false,
        @ColumnInfo(name = "lastSavedPosition") val lastSavedPosition: Int = 0
    ): Serializable
}