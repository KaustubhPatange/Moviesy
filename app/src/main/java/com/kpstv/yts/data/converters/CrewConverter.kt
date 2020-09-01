package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.data.models.Crew
import java.lang.reflect.Type

object CrewConverter {
    @TypeConverter
    @JvmStatic
    fun fromCrewListToString(crew: List<Crew?>?): String? {
        if (crew == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Crew?>?>() {}.getType()
        return gson.toJson(crew, type)
    }

    @TypeConverter
    @JvmStatic
    fun toCrewListString(crewString: String?): List<Crew>? {
        if (crewString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Crew?>?>() {}.getType()
        return gson.fromJson<List<Crew>>(crewString, type)
    }
}