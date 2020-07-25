package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.data.models.AppDatabase
import java.lang.reflect.Type

object AppDatabaseConverter {
    @TypeConverter
    @JvmStatic
    fun fromAppDatabaseToString(data: AppDatabase?): String? {
        if (data == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<AppDatabase?>() {}.type
        return gson.toJson(data, type)
    }

    @TypeConverter
    @JvmStatic
    fun toAppDatabaseFromString(data: String?): AppDatabase? {
        if (data == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<AppDatabase?>() {}.type
        return gson.fromJson<AppDatabase>(data, type)
    }
}