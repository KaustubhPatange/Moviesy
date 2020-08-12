package com.kpstv.yts.extensions.utils

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.kpstv.common_moviesy.extensions.await
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.models.MovieShort
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YTSFeaturedUtils @Inject constructor(
    private val retrofitUtils: RetrofitUtils
) {
    suspend fun fetch(): ArrayList<MovieShort> {
        val list = ArrayList<MovieShort>()
        try {
            val client = OkHttpClient.Builder()
                .callTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val response =
                client.newCall(Request.Builder().url(AppInterface.YTS_BASE_URL).build()).await()
            if (!response.isSuccessful) return list

            val doc = Jsoup.parse(response.body?.string())
            val elements = doc.getElementsByClass("browse-movie-link")

            response.close() // Always close the response when not needed

            for (i in 0..3) {
                val link = elements[i].attr("href").toString()
                val subResponse = client.newCall((Request.Builder().url(link).build())).await()
                if (!subResponse.isSuccessful) return list
                val subDoc = Jsoup.parse(subResponse.body?.string())

                subResponse.close() // Always close the response when not needed

                val movieId =
                    subDoc.getElementById("movie-info").attr("data-movie-id").toString().toInt()
                var imdbCode = ""
                var rating = 0.0
                subDoc.getElementsByClass("rating-row").forEach { row ->
                    if (row.hasAttr("itemscope")) {
                        imdbCode = row.getElementsByClass("icon")[0]
                            .attr("href").toString().split("/")[4]
                        row.allElements.forEach { element ->
                            if (element.hasAttr("itemprop") && element.attr("itemprop")
                                    .toString() == "ratingValue"
                            ) {
                                rating = element.ownText().toDouble()
                            }
                        }
                    }
                }

                var title = ""
                var year = 0
                var bannerUrl = ""
                var runtime = 0

                subDoc.getElementById("mobile-movie-info").allElements.forEach {
                    if (it.hasAttr("itemprop"))
                        title = it.ownText()
                    else
                        if (it.ownText().isNotBlank() && it.ownText().isDigitsOnly())
                            year = it.ownText().toInt()
                }

                subDoc.getElementById("movie-poster").allElements.forEach {
                    if (it.hasAttr("itemprop"))
                        bannerUrl = it.attr("src").toString()
                }

                subDoc.getElementsByClass("icon-clock")[0]?.let {
                    val runtimeString = it.parent().ownText().trim()
                    if (runtimeString.contains("hr")) {
                        runtime = runtimeString.split("hr")[0].trim().toInt() * 60
                        if (runtimeString.contains("min"))
                            runtime += runtimeString.split(" ")[2].trim().toInt()
                        return@let
                    }
                    if (runtimeString.contains("min"))
                        runtime += runtimeString.split("min")[0].trim().toInt()
                }

                list.add(
                    MovieShort(movieId, link, title, year, rating, runtime, imdbCode, bannerUrl)
                )
            }
        }catch (e: Exception) {
            Log.e(javaClass.simpleName, "Featured fetch failed", e)
        }
        return list
    }
}