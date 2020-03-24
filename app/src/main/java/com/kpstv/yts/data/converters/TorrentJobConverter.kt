package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.models.Torrent
import com.kpstv.yts.models.TorrentJob
import java.lang.reflect.Type

object TorrentJobConverter {
    @TypeConverter
    @JvmStatic
    fun fromTorrentJobToString(torrentJob: TorrentJob?): String? {
        if (torrentJob == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<TorrentJob?>() {}.type
        return gson.toJson(torrentJob, type)
    }

    @TypeConverter
    @JvmStatic
    fun toTorrentJobfromString(torrentJobString: String?): TorrentJob? {
        if (torrentJobString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<TorrentJob?>() {}.type
        return gson.fromJson<TorrentJob>(torrentJobString, type)
    }
}