package com.kpstv.yts.models.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.models.TmDbCast
import com.kpstv.yts.models.TmDbMovie
import com.kpstv.yts.models.data.data_movie

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
        val imageUrl: String
    )

    /**
     * var title: String, var banner_url: String,
    val url: String, val hash: String, val quality: String,
    val type: String, val seeds: Int, val peers: Int,
    @SerializedName("size") val size_pretty: String,
    @SerializedName("size_bytes") val size: Long,
    val date_uploaded: String, val date_uploaded_unix: String
     * */

    @Entity(tableName = "table_download")
    data class response_download(
        @PrimaryKey(autoGenerate = true)
        val id: Int? = null,
        val title: String,
        val imagePath: String?,
        val downloadPath: String?,
        val size: Long,
        val date_downloaded: String?,
        val total_video_length: Long,
        val hash: String
        )
}