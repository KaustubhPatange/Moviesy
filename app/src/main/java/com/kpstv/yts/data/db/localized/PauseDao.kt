package com.kpstv.yts.data.db.localized

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kpstv.yts.data.models.response.Model

@Dao
interface PauseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: Model.response_pause)

    @Query("select * from table_pause where hash = :hash")
    fun getTorrentJob(hash: String): Model.response_pause

    @Delete
    fun delete(data: Model.response_pause)

    @Query("select * from table_pause")
    fun getAllData(): LiveData<List<Model.response_pause>>
}