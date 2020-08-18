package com.kpstv.yts.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.response.Model
import java.lang.reflect.Type

object TMdbCastMovieConverter {
    @TypeConverter
    @JvmStatic
    fun fromTMdbCastListToString(tmdbMovies: List<Model.response_cast_movie.Cast?>?): String? {
        if (tmdbMovies == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Model.response_cast_movie.Cast?>?>() {}.type
        return gson.toJson(tmdbMovies, type)
    }

    @TypeConverter
    @JvmStatic
    fun toTMdbCastListString(movieString: String?): List<Model.response_cast_movie.Cast>? {
        if (movieString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<Model.response_cast_movie.Cast?>?>() {}.type
        return gson.fromJson<List<Model.response_cast_movie.Cast>>(movieString, type)
    }
}