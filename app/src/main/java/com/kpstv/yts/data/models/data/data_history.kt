package com.kpstv.yts.data.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.yts.AppInterface
import java.util.*

@Entity(tableName = "table_history")
data class data_history(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val query: String,
    val time: Long
) {
    companion object {
        fun from(query: String) =
            data_history(
                query = query,
                time = AppInterface.HistoryDateFormatter.format(Calendar.getInstance().time).toLong()
            )
    }
}