package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.models.MovieShort
import java.lang.reflect.Type

object MovieShortConverter {
    @TypeConverter
    @JvmStatic
    fun fromMovieShortListToString(movies: ArrayList<MovieShort?>?): String? {
        if (movies == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<MovieShort?>?>() {}.getType()
        return gson.toJson(movies, type)
    }

    @TypeConverter
    @JvmStatic
    fun toMovieShortListString(movieString: String?): ArrayList<MovieShort>? {
        if (movieString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<MovieShort?>?>() {}.getType()
        return gson.fromJson<ArrayList<MovieShort>>(movieString, type)
    }
}