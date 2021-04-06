package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.data.models.Torrent
import java.lang.reflect.Type

object TorrentListConverter {
    @TypeConverter
    @JvmStatic
    fun fromTorrentListToString(torrents: ArrayList<Torrent?>?): String? {
        if (torrents == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Torrent?>?>() {}.type
        return gson.toJson(torrents, type)
    }

    @TypeConverter
    @JvmStatic
    fun toTorrentListfromString(torrentString: String?): ArrayList<Torrent>? {
        if (torrentString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Torrent?>?>() {}.type
        return gson.fromJson<ArrayList<Torrent>>(torrentString, type)
    }
}