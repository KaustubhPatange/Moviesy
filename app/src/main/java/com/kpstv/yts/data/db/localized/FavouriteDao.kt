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

    @Delete
    fun delete(data: Model.response_favourite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllData(quotes : List<Model.response_favourite>)
}