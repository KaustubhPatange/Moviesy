package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.data.models.data.data_history

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: data_history)

    @Delete
    fun delete(data: data_history)

    @Query("select * from table_history where `query` = :query")
    fun getData(query: String): data_history?

    @Query("select * from table_history order by id desc limit :limit")
    suspend fun getAllData(limit: Int): List<data_history>
}