package com.kpstv.yts.extensions.utils

import androidx.core.text.isDigitsOnly
import com.kpstv.common_moviesy.extensions.await
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.db.localized.MainDao
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.Result
import com.kpstv.yts.data.models.Subtitle
import com.kpstv.yts.extensions.errors.SSLHandshakeException
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class YTSParser @Inject constructor(
    retrofitUtils: RetrofitUtils
) {

    private val client = retrofitUtils.getHttpBuilder()
        .callTimeout(0, TimeUnit.SECONDS)
        .connectTimeout(0, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    /**
     * Fetch featured movies
     */
    suspend fun fetchFeaturedMovies(): ArrayList<MovieShort> {
        val list = ArrayList<MovieShort>()
        val response =
            client.newCall(Request.Builder().url(AppInterface.YTS_BASE_URL).build()).await()
        if (response.code == 525) throw SSLHandshakeException("Could not establish successful connection with server")
        if (!response.isSuccessful) return list

        val doc = Jsoup.parse(response.body?.string())
        val elements = doc.getElementsByClass("browse-movie-link")

        response.close() // Always close the response when not needed

        for (i in 0..3) {
            val link = elements[i].attr("href").toString()
            parseMovieUrl(link)?.let { list.add(it) }
        }
        return list
    }

    /**
     * Fetch upcoming movies
     */
    suspend fun fetchUpcomingMovies(): ArrayList<MovieShort> {
        val list = ArrayList<MovieShort>()
        val response =
            client.newCall(Request.Builder().url(AppInterface.YTS_BASE_URL).build()).await()
        if (response.code == 525) throw SSLHandshakeException("Could not establish successful connection with server")
        if (!response.isSuccessful) return list

        val doc = Jsoup.parse(response.body?.string())
        response.close() // Always close the response when not needed

        val homeMovies = doc.getElementsByClass("home-movies")?.firstOrNull { it.html().contains(">Upcoming YIFY Movies ") }
            ?.child(1)?.children() ?: return arrayListOf()

        for (i in 0 until homeMovies.size) {
            val element = homeMovies[i]

            var url: String? = null
            var imDbCode: String? = null
            var rating = 0.0

            val bannerElement = element.getElementsByClass("browse-movie-link").firstOrNull() ?: continue
            val targetUrl = bannerElement.attr("href")
            if (targetUrl.contains("yts")) {
                url = targetUrl
            } else if (targetUrl.contains("imdb.com")) {
                imDbCode = targetUrl.split("/")[4]
            }

            val ratingElement = element.getElementsByClass("rating")?.firstOrNull()
            if (ratingElement != null) {
                rating = ratingElement.text().split("/")[0].trim().toDouble()
            }

            val imageElement = element.getElementsByClass("img-responsive")?.firstOrNull() ?: continue
            val imageUrl = AppInterface.YTS_BASE_URL + imageElement.attr("src")

            val titleElement = element.getElementsByClass("browse-movie-title")?.firstOrNull() ?: continue
            val title = titleElement.text()

            val yearElement = element.getElementsByClass("browse-movie-year")?.firstOrNull() ?: continue
            val year = yearElement.text().split("\\s".toRegex())[0].trim().toInt()
            val progressItem = yearElement.text().replace(year.toString(), "").trim()
            val progressValue = yearElement.getElementsByTag("progress")?.firstOrNull()?.attr("value")?.toInt() ?: continue

            val movieShort = MovieShort(
                url = url,
                imdbCode = imDbCode,
                title = title,
                rating = rating,
                bannerUrl = imageUrl,
                year = year,
                progress = MovieShort.Progress(progressItem, progressValue),
                runtime = 0
            )

            list.add(movieShort)
        }

        return list
    }

    /**
     * Parse a movie from url
     */
    suspend fun parseMovieUrl(link: String): MovieShort? {
        try {
            val subResponse = client.newCall((Request.Builder().url(link).build())).await()
            if (!subResponse.isSuccessful) return null
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

            return MovieShort(movieId, link, title, year, rating, runtime, imdbCode, bannerUrl)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Fetch subtitles from imdb Code
     */
    suspend fun fetchSubtitles(imDbCode: String): SubtitleResult {
        val list = ArrayList<Subtitle>()
        var hasArabic = false
        var hasEnglish = false
        var hasSpanish = false

        try {
            val response = client.newCall(
                Request.Builder().url("${AppInterface.YIFY_BASE_URL}/movie-imdb/${imDbCode}")
                    .build()
            ).await()

            val subtitles = Jsoup.parse(response.body?.string()).getElementsByClass("high-rating")
            response.close() // Always close the response when not needed

            for (element in subtitles) {
                val a = element.select("a")[0]

                val country = element.getElementsByClass("sub-lang")[0].ownText()
                if (country == "Arabic") hasArabic = true
                if (country == "English") hasEnglish = true
                if (country == "Spanish") hasSpanish = true

                list.add(
                    Subtitle(
                        country,
                        a.ownText(),
                        element.getElementsByClass("label")[0].ownText().toInt(),
                        element.getElementsByClass("uploader-cell")[0].ownText(),
                        "${AppInterface.YIFY_BASE_URL}${a.attr("href")}"
                    )
                )
            }
            return SubtitleResult.Success(list, hasArabic, hasEnglish, hasSpanish)
        } catch (e: Exception) {
            return SubtitleResult.Error(e)
        }
    }

    /**
     * Parse the subtitle link
     */
    suspend fun parseSubtitleLink(endPoint: String): Result<String> {
        return try {
            val response = client.newCall(
                Request.Builder().url(endPoint).build()
            ).await()

            val link = Jsoup.parse(response.body?.string()).getElementsByClass("download-subtitle")[0]
                .attr("href").toString();

            response.close()
            Result.success(link)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    suspend fun isMovieFetchNeeded(identifier: String, repository: MainDao): Boolean {
        try {
            val movieModel = repository.getMoviesByQuery(identifier)
            movieModel?.also {
                val currentCalender = Calendar.getInstance()
                currentCalender.add(Calendar.HOUR, -AppInterface.QUERY_SPAN_DIFFERENCE_UPCOMING)
                val currentSpan = AppInterface.MainDateFormatter.format(
                    currentCalender.time
                ).toLong()
                return if (currentSpan > movieModel.time) {
                    repository.deleteMovie(movieModel)
                    true
                } else false
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }
}

sealed class SubtitleResult {
    data class Success(
        val list: List<Subtitle>,
        val hasArabic: Boolean,
        val hasEnglish: Boolean,
        val hasSpanish: Boolean
    ) : SubtitleResult()
    data class Error(val ex: Exception) : SubtitleResult()
}