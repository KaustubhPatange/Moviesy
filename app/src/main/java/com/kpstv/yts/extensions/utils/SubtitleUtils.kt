package com.kpstv.yts.extensions.utils

import android.util.Log
import com.kpstv.yts.data.models.SubHolder
import java.io.FileReader
import java.lang.StringBuilder

class SubtitleUtils {
   companion object {
       private val TAG = "SubtitleUtils"
       fun parseSubtitles(file: String): ArrayList<SubHolder> {
           val list = ArrayList<SubHolder>()

           val reader = FileReader(file)
           val totalLines = ArrayList(reader.readLines()).apply { add("") }

           var i=0
           while (i<totalLines.size) {
               val it = totalLines[i]
               if (it.contains(":") && it.contains(" --> ")) {
                   val firstFrame = it.split(" --> ")[0].trim()
                   val secondFrame = it.split(" --> ")[1].trim()
                   i++
                   try {
                       val builder = StringBuilder()
                       while (totalLines[i].isNotBlank()) {
                           builder.append(totalLines[i]).append("\n")
                           i++
                       }
                       list.add(SubHolder(builder.toString().trim(), parseTimeFrame(firstFrame), parseTimeFrame(secondFrame)))
                   } catch (e: Exception) {
                       Log.e(TAG, "Subtitle crashed ${e.message}", e)
                   }
               }else i++
           }
           return list
       }

       private fun parseTimeFrame(it: String): Int {
           val millis = it.substring(it.length - 3).toInt()
           val seconds = it.substring(6,8).toInt()
           val minutes = it.substring(3,5).toInt() * 60
           val hours = it.substring(0,2).toInt() * 60 * 60

           return ((hours + seconds + minutes)*1000)+millis
       }
   }
}