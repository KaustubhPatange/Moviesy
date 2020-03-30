package com.kpstv.yts.data.db.localized

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kpstv.yts.models.data.data_main
import com.kpstv.yts.models.response.Model

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: Model.response_download)

    @Query("select * from table_download where hash = :hash")
    fun getDownload(hash: String): Model.response_download

    @Query("update table_download set recentlyPlayed = :updateRecentlyPlayed, lastSavedPosition = :updateLastPosition where hash = :hash")
    fun updateDownload(hash: String, updateRecentlyPlayed: Boolean, updateLastPosition: Int)

    @Delete()
    fun delete(data: Model.response_download)

    @Query("select * from table_download")
    fun getAllDownloads(): LiveData<List<Model.response_download>>
}