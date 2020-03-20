package com.kpstv.yts.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.models.Movie
import com.kpstv.yts.models.MovieShort

@Entity(tableName = "table_main")
data class data_main (
    @PrimaryKey(autoGenerate = true)
    val id: Int?=null,
    val time: Long,
    val movies: ArrayList<MovieShort>,
    val query: String,
    val isMore: Boolean
)