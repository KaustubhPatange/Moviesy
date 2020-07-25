package com.kpstv.yts.data.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.data.models.MovieShort

@Entity(tableName = "table_main")
data class data_main (
    @PrimaryKey(autoGenerate = true)
    val id: Int?=null,
    val time: Long,
    val movies: ArrayList<MovieShort>,
    val query: String,
    val isMore: Boolean
)