package com.kpstv.yts.data.db.localized

import androidx.room.*
import com.kpstv.yts.models.response.Model

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: Model.response_favourite)

    @Query("select * from table_favourites where movieId = :id")
    fun getData(id: Int): Model.response_favourite

    @Query("select * from table_favourites")
    fun getAllData(): List<Model.response_favourite>

    @Delete
    fun delete(data: Model.response_favourite)
}