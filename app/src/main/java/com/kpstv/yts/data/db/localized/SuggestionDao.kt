package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.data.models.data.data_tmdb

@Dao
interface SuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovies(data: data_tmdb)

    @Query("select * from table_dataTMDB where imdbCode = :imdbCode")
    suspend fun getMoviesByImDb(imdbCode: String): data_tmdb?

    @Delete
    suspend fun deleteMovies(data: data_tmdb)
}