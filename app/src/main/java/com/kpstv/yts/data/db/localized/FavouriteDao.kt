package com.kpstv.yts.data.db.localized

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kpstv.yts.data.models.response.Model

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: Model.response_favourite)

    @Query("select * from table_favourites where movieId = :id")
    fun getData(id: Int): Model.response_favourite?

    @Query("select * from table_favourites")
    fun getAllData(): LiveData<List<Model.response_favourite>>

    @Query("delete from table_favourites where movieId = :movieId")
    suspend fun delete(movieId: Int)

    @Delete
    suspend fun delete(data: Model.response_favourite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllData(quotes : List<Model.response_favourite>)

    @Query("select exists(select 1 from table_favourites where movieId = :movieId limit 1)")
    fun isDataExistLive(movieId: Int): LiveData<Boolean>

    @Query("select exists(select 1 from table_favourites where movieId = :movieId limit 1)")
    fun isDataExist(movieId: Int): Boolean
}