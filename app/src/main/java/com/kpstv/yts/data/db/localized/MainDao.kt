package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.data.models.data.data_main

@Dao
interface MainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: data_main)

    @Transaction
    suspend fun saveMovies(dataMain: data_main) {
        removeMoviesByQuery(dataMain.query)
        upsert(dataMain)
    }

    @Query("select * from table_main where `query` = :query")
    suspend fun getMoviesByQuery(query: String): data_main?

    @Query("delete from table_main where `query` = :query")
    suspend fun removeMoviesByQuery(query: String)

    @Delete
    suspend fun deleteMovie(data: data_main)
}