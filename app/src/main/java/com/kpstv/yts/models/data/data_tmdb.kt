package com.kpstv.yts.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.models.TmDbMovie

@Entity(tableName = "table_dataTMDB")
data class data_tmdb (
    @PrimaryKey(autoGenerate = true)
    val uid :Int?=null,
    val imdbCode: String,
    val movies: ArrayList<TmDbMovie>,
    val isMore: Boolean,
    val time: Long,
    val tag: String?=null
)