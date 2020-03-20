package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.models.TmDbMovie
import java.lang.reflect.Type

object TMdbConverter {
    @TypeConverter
    @JvmStatic
    fun fromTMdbMovieListToString(tmdbMovies: ArrayList<TmDbMovie?>?): String? {
        if (tmdbMovies == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<TmDbMovie?>?>() {}.type
        return gson.toJson(tmdbMovies, type)
    }

    @TypeConverter
    @JvmStatic
    fun toTMdbMovieListString(movieString: String?): ArrayList<TmDbMovie>? {
        if (movieString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<TmDbMovie?>?>() {}.type
        return gson.fromJson<ArrayList<TmDbMovie>>(movieString, type)
    }
}