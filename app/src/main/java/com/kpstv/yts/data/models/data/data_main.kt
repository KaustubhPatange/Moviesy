package com.kpstv.yts.data.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.models.MovieShort
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "table_main")
data class data_main (
    @PrimaryKey(autoGenerate = true)
    val id: Int?=null,
    val time: Long,
    val movies: ArrayList<MovieShort>,
    val query: String,
    val isMore: Boolean
) {
    companion object {
        fun from(data: ArrayList<MovieShort>, query: String, isMore: Boolean = false) =
            data_main(
                time = AppInterface.MainDateFormatter.format(Calendar.getInstance().time).toLong(),
                movies = data,
                query = query,
                isMore = isMore
            )
    }
}