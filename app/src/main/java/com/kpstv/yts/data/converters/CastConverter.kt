package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.data.models.Cast
import java.lang.reflect.Type

object CastConverter {
    @TypeConverter
    @JvmStatic
    fun fromCastListToString(casts: List<Cast?>?): String? {
        if (casts == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Cast?>?>() {}.getType()
        return gson.toJson(casts, type)
    }

    @TypeConverter
    @JvmStatic
    fun toCastListString(castString: String?): List<Cast>? {
        if (castString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Cast?>?>() {}.getType()
        return gson.fromJson<List<Cast>>(castString, type)
    }
}