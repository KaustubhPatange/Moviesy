package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.models.Torrent
import java.lang.reflect.Type


object TorrentConverter {
    @TypeConverter
    @JvmStatic
    fun fromTorrentListToString(torrents: ArrayList<Torrent?>?): String? {
        if (torrents == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Torrent?>?>() {}.getType()
        return gson.toJson(torrents, type)
    }

    @TypeConverter
    @JvmStatic
    fun toTorrentListfromString(torrentString: String?): ArrayList<Torrent>? {
        if (torrentString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Torrent?>?>() {}.getType()
        return gson.fromJson<ArrayList<Torrent>>(torrentString, type)
    }
}