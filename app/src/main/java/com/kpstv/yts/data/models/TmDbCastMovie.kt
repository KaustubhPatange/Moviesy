package com.kpstv.yts.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.data.models.response.Model

@Entity(tableName = "table_castmovies")
data class TmDbCastMovie(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val personId: Int,
    val name: String,
    val movies: List<Model.response_cast_movie.Cast>
) {
    companion object {
        fun from(cast: TmDbCast, movies: List<Model.response_cast_movie.Cast>) =
            TmDbCastMovie(
                personId = cast.personId,
                name = cast.name,
                movies = movies
            )
    }
}