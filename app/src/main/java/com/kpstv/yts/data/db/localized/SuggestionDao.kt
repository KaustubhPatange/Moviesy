package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.data.models.data.data_tmdb

@Dao
interface SuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: data_tmdb)

    @Query("select * from table_dataTMDB where imdbCode = :imdbCode")
    fun getMovieData(imdbCode: String): data_tmdb

    @Delete
    fun delete(data: data_tmdb)
}