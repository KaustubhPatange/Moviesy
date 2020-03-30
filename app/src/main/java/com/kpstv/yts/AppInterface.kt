package com.kpstv.yts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Environment
import androidx.core.text.isDigitsOnly
import com.danimahardhika.cafebar.CafeBar
import com.kpstv.yts.extensions.add
import com.kpstv.yts.fragments.GenreFragment
import com.kpstv.yts.interfaces.listener.ObservableListener
import com.kpstv.yts.models.MovieShort
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.concurrent.Callable

class AppInterface {
    companion object {
        private var TAG = "AppInterface"

        var YIFY_BASE_URL = "https://yts-subs.com"
        var YTS_BASE_URL = "https://yts.mx"
        var YTS_BASE_API_URL = "$YTS_BASE_URL/api/v2/"
        var TMDB_BASE_URL = "https://api.themoviedb.org/3/"
        var TMDB_IMAGE_PREFIX = "https://image.tmdb.org/t/p/w500"
        var TMDB_API_KEY = "dbaba3594e59e4ff47c003b2ddb82c2a"
        var COUNTRY_FLAG_JSON_URL = "https://pastebin.com/raw/H0CYRdJ9"
        var SUGGESTION_URL =
            "https://suggestqueries.google.com/complete/search?ds=yt&client=firefox&q="
        var STORAGE_LOCATION =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var STREAM_LOCATION = "torrents"
        var SUBTITLE_LOCATION = File(Environment.getExternalStorageDirectory(), "Subtitles")
        var ANONYMOUS_TORRENT_DOWNLOAD = true
        var DOWNLOAD_TIMEOUT_SECOND = 40
        var DOWNLOAD_CONNECTION_TIMEOUT = 100
        var MOVIE_SPAN_DIFFERENCE = 3
        var QUERY_SPAN_DIFFERENCE = 6
        var CUSTOM_LAYOUT_YTS_SPAN = 8
        const val MOVIE_FETCH_SIZE = 10
        var IS_DARK_THEME = true

        const val TORRENT_NOT_SUPPORTED = "com.kpstv.yts.TORRENT_NOT_SUPPORTED"
        const val MODEL_UPDATE = "com.kpstv.yts.MODEL_UPDATE"
        const val STOP_SERVICE = "com.kpstv.yts.STOP_SERVICE"
        const val PENDING_JOB_UPDATE = "com.kpstv.yts.PENDING_JOB_UPDATE"
        const val EMPTY_QUEUE = "com.kpstv.yts.EMPTY_QUEUE"
        const val PAUSE_JOB = "com.kpstv.yts.PAUSE_JOB"
        const val UNPAUSE_JOB = "com.kpstv.yts.ADD_ONLY_JOB"
        const val REMOVE_JOB = "com.kpstv.yts.REMOVE_JOB"
        const val REMOVE_CURRENT_JOB = "com.kpstv.yts.REMOVE_CURRENT_JOB"

        val GENRE_CATEGORY_LIST = ArrayList<GenreFragment.LocalGenreModel>().apply {
            add("Action", R.drawable.ic_action_genre, YTSQuery.Genre.action)
            add("Adventure", R.drawable.ic_adventure_genre, YTSQuery.Genre.adventure)
            add("Animation", R.drawable.ic_animation_genre, YTSQuery.Genre.animation)
            add("Comedy", R.drawable.ic_comedy_genre, YTSQuery.Genre.comedy)
            add("Crime", R.drawable.ic_crime_genre, YTSQuery.Genre.crime)
            add("Documentary", R.drawable.ic_documentary_genre, YTSQuery.Genre.documentary)
            add("Drama", R.drawable.ic_drama_genre, YTSQuery.Genre.drama)
            add("Family", R.drawable.ic_animation_genre, YTSQuery.Genre.family)
            add("Fantasy", R.drawable.ic_fantasy_genre, YTSQuery.Genre.fantasy)
            add("History", R.drawable.ic_history_genre, YTSQuery.Genre.history)
            add("Horror", R.drawable.ic_horror_genre, YTSQuery.Genre.horror)
            add("Musical", R.drawable.ic_musical_genre, YTSQuery.Genre.musical)
            add("Romance", R.drawable.ic_romance_genre, YTSQuery.Genre.romance)
            add("Sci-Fi", R.drawable.ic_sci_fi_genre, YTSQuery.Genre.sci_fi)
            add("Sports", R.drawable.ic_sport_genre, YTSQuery.Genre.sport)
            add("Thriller", R.drawable.ic_thriller_genre, YTSQuery.Genre.thriller)
            add("Western", R.drawable.ic_western_genre, YTSQuery.Genre.western)
        }


        @SuppressLint("SimpleDateFormat")
        val MainDateFormatter = SimpleDateFormat("yyyyMMddHH")

        fun handleRetrofitError(context: Context, t: Exception?) {
            var message = "Site is not responding. Try to change proxy from settings."
            if (t?.message == null) {
                message = "Error: Unknown, could not be determined"
            } else if (!t.message?.contains("timeout")!!) {
                message = "Error: ${t.message}"
            }
            CafeBar.builder(context)
                .content(message)
                .floating(true)
                .duration(CafeBar.Duration.INDEFINITE)
                .neutralText("Dismiss")
                .onNeutral {
                    if ((context as Activity?) != null) {
                        context.finish()
                    }
                }
                .autoDismiss(false)
                .showShadow(true)
                .show();
        }


        fun formatDownloadSpeed(downloadSpeed: Float): String {
            val speed = downloadSpeed.toDouble() / 1000.00
            return if (speed > 1000) {
                DecimalFormat("0.0").format(speed / 1000) + " MB/s"
            } else DecimalFormat("0.0").format(speed) + " KB/s"
        }

        @SuppressLint("CheckResult")
        fun getPopularUtils(listener: ObservableListener): Disposable {
            return Observable.fromCallable(Callable<ArrayList<MovieShort>> {
                val list = ArrayList<MovieShort>()
                val doc = Jsoup.connect(YTS_BASE_URL).get()
                val elements = doc.getElementsByClass("browse-movie-link")
                for (i in 0..3) {
                    val link = elements[i].attr("href").toString()
                    val subDoc = Jsoup.connect(link).get()

                    val movieId =
                        subDoc.getElementById("movie-info").attr("data-movie-id").toString().toInt()
                    var imdbCode = ""
                    var rating = 0.0
                    subDoc.getElementsByClass("rating-row").forEach {
                        if (it.hasAttr("itemscope")) {
                            imdbCode = it.getElementsByClass("icon")[0]
                                .attr("href").toString().split("/")[4]
                            it.allElements.forEach {
                                if (it.hasAttr("itemprop") && it.attr("itemprop")
                                        .toString() == "ratingValue"
                                ) {
                                    rating = it.ownText().toDouble()
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
                return@Callable list
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listener.onSuccess(it) }, { listener.onError(it) })
        }
    }
}