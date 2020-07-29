package com.kpstv.yts.extensions.utils

import android.content.Context
import com.google.gson.Gson
import com.kpstv.yts.data.models.data.data_genre_recommend
import com.kpstv.yts.data.models.internel.GenreRecommend
import java.io.File
import kotlin.collections.ArrayList

// TODO: See if you need to add some recommendations based on movie
class RecommendUtils {
    companion object {
        fun writeGenreUtils(context: Context, genre: String, movieTitle: String) {
            val gson = Gson()
            var genres = getGenreUtils(context)
            if (genres?.list?.isNotEmpty() == true) {
                for (c in genres.list) {
                    if (c.genre == genre && !c.movieNames.contains(movieTitle)) {
                        c.times++
                        genres.timesWritten++
                    }else {
                        val innerMovieList = ArrayList<String>()
                        innerMovieList.add(movieTitle)
                        genres.list.add(GenreRecommend(genre,1,innerMovieList))
                    }
                }
            }else {
                val innerMovieList = ArrayList<String>()
                val innerList = ArrayList<GenreRecommend>()
                innerMovieList.add(movieTitle)
                innerList.add(GenreRecommend(genre,1,innerMovieList))
                genres = data_genre_recommend(1,innerList)
            }
            val jsonString = Gson().toJson(genres)
            File(context.filesDir, "genre_recommend.json").writeText(jsonString)
        }

        fun getTopGenre(context: Context): ArrayList<String>? {
            var genres = getGenreUtils(context)
            return if (genres?.list?.isNotEmpty() == true) {
                if (genres.timesWritten>3) {
                    val list = ArrayList<String>()
                    val sortedList = genres.list.sortedWith(compareByDescending { it.times })
                    if (sortedList.size>3) {
                        list.add(sortedList[0].genre)
                        list.add(sortedList[1].genre)
                        list.add(sortedList[2].genre)
                    }else list.add(sortedList[0].genre)
                    list
                }else null
            }else null
        }

        private fun getGenreUtils(context: Context): data_genre_recommend? {
            val file = File(context.filesDir, "genre_recommend.json")
            return if (file.exists()) {
                Gson().fromJson<data_genre_recommend>(file.readText(), data_genre_recommend::class.java)
            }else null
        }
    }
}

