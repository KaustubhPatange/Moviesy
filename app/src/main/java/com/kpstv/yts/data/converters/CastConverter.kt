package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.models.Cast
import java.lang.reflect.Type

object CastConverter {
    @TypeConverter
    @JvmStatic
    fun fromCastListToString(casts: ArrayList<Cast?>?): String? {
        if (casts == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Cast?>?>() {}.getType()
        return gson.toJson(casts, type)
    }

    @TypeConverter
    @JvmStatic
    fun toCastListString(castString: String?): ArrayList<Cast>? {
        if (castString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Cast?>?>() {}.getType()
        return gson.fromJson<ArrayList<Cast>>(castString, type)
    }
}