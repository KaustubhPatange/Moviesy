package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.data.models.Torrent
import java.lang.reflect.Type

object TorrentConverter {
    @TypeConverter
    @JvmStatic
    fun fromTorrentJobToString(torrentJob: Torrent?): String? {
        if (torrentJob == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Torrent?>() {}.type
        return gson.toJson(torrentJob, type)
    }

    @TypeConverter
    @JvmStatic
    fun toTorrentJobfromString(torrentJobString: String?): Torrent? {
        if (torrentJobString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Torrent?>() {}.type
        return gson.fromJson<Torrent>(torrentJobString, type)
    }
}