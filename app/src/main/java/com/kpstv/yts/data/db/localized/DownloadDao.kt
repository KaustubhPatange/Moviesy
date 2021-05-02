package com.kpstv.yts.data.db.localized

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kpstv.yts.data.models.response.Model

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: Model.response_download)

    @Query("select * from table_download where hash = :hash")
    fun getDownload(hash: String): Model.response_download?

    @Query("select * from table_download where movieId = :movieId")
    suspend fun getDownload(movieId: Int): Model.response_download?

    @Query("update table_download set recentlyPlayed = :updateRecentlyPlayed, lastSavedPosition = :updateLastPosition where hash = :hash")
    fun updateDownload(hash: String, updateRecentlyPlayed: Boolean, updateLastPosition: Int)

    @Query("update table_download set recentlyPlayed = :updateRecentlyPlayed where hash = :hash")
    suspend fun updateDownload(hash: String, updateRecentlyPlayed: Boolean)

    @Delete
    fun delete(data: Model.response_download)

    @Query("select * from table_download")
    fun getAllLiveDownloads(): LiveData<List<Model.response_download>>

    @Query("select * from table_download")
    fun getAllDownloads(): List<Model.response_download>
}