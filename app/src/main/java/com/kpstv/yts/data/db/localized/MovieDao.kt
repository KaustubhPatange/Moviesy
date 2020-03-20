package com.kpstv.yts.data.db.localized

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.yts.models.Movie

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(movie: Movie)

    @Query("select * from table_movie where id = :movieId")
    fun getMovieById(movieId: Int): Movie

    /** Eg title_long = name (year) */
    @Query("select * from table_movie where title_long = :queryString")
    fun getMovieByTitleLong(queryString: String): Movie
}