package com.kpstv.yts.data.converters

import androidx.room.TypeConverter

object GenreConverter {
    @TypeConverter
    @JvmStatic
    fun fromListToString(genres: ArrayList<String?>?): String? {
        if (genres==null) {
            return null
        }
        return genres.joinToString(",")
    }

    @TypeConverter
    @JvmStatic
    fun toListfromString(genreString: String?): ArrayList<String>? {
        if (genreString==null) {
            return null
        }
        return ArrayList(genreString.split(","))
    }
}