package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.data.models.data.data_main

@Dao
interface MainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: data_main)

    @Query("select * from table_main where `query` = :query")
    fun getMovies(query: String): data_main?

    @Query("delete from table_main where `query` = :query")
    fun deleteMovie(query: String)

    @Delete
    fun delete(data: data_main)
}