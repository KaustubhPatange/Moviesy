package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.models.data.data_main
import com.kpstv.yts.models.response.Model

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: Model.response_download)

    @Query("select * from table_download where hash = :hash")
    fun getDownload(hash: String): Model.response_download

    @Delete()
    fun delete(data: Model.response_download)

    @Query("select * from table_download")
    fun getAllDownloads(): List<Model.response_download>
}