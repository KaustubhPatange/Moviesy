package com.kpstv.yts.data.converters

import com.kpstv.yts.YTSQuery
import java.lang.Exception

object GenreEnumConverter {

    @JvmStatic
    fun toGenrefromString(text: String): YTSQuery.Genre? {
        return try {
            var genre = text.toLowerCase()
            if (genre == "sci-fi") genre = "sci_fi"
            YTSQuery.Genre.valueOf(genre)
        }catch (e: Exception) {
            null
        }
    }
}