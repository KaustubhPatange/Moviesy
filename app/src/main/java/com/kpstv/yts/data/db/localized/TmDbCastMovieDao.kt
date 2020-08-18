package com.kpstv.yts.data.db.localized

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.yts.data.models.TmDbCastMovie

@Dao
interface TmDbCastMovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: TmDbCastMovie)

    @Query("select * from table_castmovies where personId = :id")
    suspend fun getMovies(id: Int): TmDbCastMovie?
}