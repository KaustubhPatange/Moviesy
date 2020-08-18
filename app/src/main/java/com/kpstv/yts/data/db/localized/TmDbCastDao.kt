package com.kpstv.yts.data.db.localized

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.yts.data.models.TmDbCastDomain

@Dao
interface TmDbCastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: TmDbCastDomain)

    @Query("select * from table_cast where imdbCode = :imdbCode")
    suspend fun getCasts(imdbCode: String): List<TmDbCastDomain>
}