package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.data.models.TmDbCast
import com.kpstv.yts.data.models.Torrent
import java.lang.reflect.Type

object TMdbCastConverter {
    @TypeConverter
    @JvmStatic
    fun fromTMdbCastToString(data: TmDbCast?): String? {
        if (data == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<TmDbCast?>() {}.type
        return gson.toJson(data, type)
    }

    @TypeConverter
    @JvmStatic
    fun toTMdbCastfromString(string: String?): TmDbCast? {
        if (string == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<TmDbCast?>() {}.type
        return gson.fromJson<TmDbCast>(string, type)
    }
}