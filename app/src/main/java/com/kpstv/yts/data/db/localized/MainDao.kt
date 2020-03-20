package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.models.Movie
import com.kpstv.yts.models.data.data_main
import com.kpstv.yts.models.response.Model
import kotlinx.coroutines.selects.select

@Dao
interface MainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: data_main)

    @Query("select * from table_main where `query` = :query")
    fun getMovies(query: String): data_main

    @Delete()
    fun delete(data: data_main)
}