package com.kpstv.yts.data.db.localized

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Crew
import com.kpstv.yts.data.models.Movie

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovie(movie: Movie)

    @Query("select * from table_movie where id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    suspend fun getCastById(movieId: Int): List<Cast>? = getMovieById(movieId)?.cast

    suspend fun getCrewById(movieId: Int): List<Crew>? = getMovieById(movieId)?.crew

    /** Eg title_long = name (year) */
    @Query("select * from table_movie where title_long = :longTitle")
    suspend fun getMovieByTitleLong(longTitle: String): Movie?
}