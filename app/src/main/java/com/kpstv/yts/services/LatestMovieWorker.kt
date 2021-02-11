package com.kpstv.yts.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.data.db.repository.MainRepository
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.data.data_main
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.ui.settings.GeneralSettingsFragment
import retrofit2.await
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit

class LatestMovieWorker @WorkerInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ytsApi: YTSApi,
    private val tmdbApi: TMdbApi,
    private val repository: MainRepository
) : CoroutineWorker(appContext, workerParams) {
    private val TAG = javaClass.simpleName
    private val preference by appContext.defaultPreference()

    override suspend fun doWork(): Result {
        val shouldWork =
            preference.getBoolean(GeneralSettingsFragment.LATEST_MOVIE_NOTIFY_PREF, true)

        if (shouldWork) {

            val query = YTSQuery.ListMoviesBuilder().apply {
                setSortBy(YTSQuery.SortBy.date_added)
            }.build()

            val previousList = repository.getMoviesByQuery(QueryConverter.fromMapToString(query))
            val response = ytsApi.listMovies(query).await()

            val isMoreAvailable = response.data.movie_count > AppInterface.CUSTOM_LAYOUT_YTS_SPAN

            val movieList = ArrayList<MovieShort>()
            response.data.movies?.take(AppInterface.CUSTOM_LAYOUT_YTS_SPAN)?.forEach {
                movieList.add(MovieShort.from(it))
            }

            repository.saveMovies(
                data_main.from(
                    movieList,
                    QueryConverter.fromMapToString(query),
                    isMoreAvailable
                )
            )

            movieList.forEach { movieShort ->
                val exist = previousList?.movies?.any { it.url == movieShort.url }
                if (exist == null || exist == false) {
                    val bannerUrl = tmdbApi.getMovie(movieShort.imdbCode!!).getBannerImage()
                    Notifications.sendMovieNotification(
                        context = applicationContext,
                        movie = movieShort,
                        posterImage = AppUtils.getBitmapFromUrl(movieShort.bannerUrl),
                        bannerImage = AppUtils.getBitmapFromUrl(bannerUrl),
                        featured = false
                    )
                }
            }
        }
        return Result.success()
    }

    companion object {
        private const val UNIQUE_ID = "latest_movie_worker"
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<LatestMovieWorker>(
                20, TimeUnit.MINUTES, 5, TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}