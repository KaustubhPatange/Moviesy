package com.kpstv.yts.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.data.db.localized.MainDao
import com.kpstv.yts.data.models.data.data_main
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.UpdateUtils
import com.kpstv.yts.extensions.utils.YTSParser
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.ui.settings.GeneralSettingsFragment
import java.util.concurrent.TimeUnit

class AppWorker @WorkerInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tmdbApi: TMdbApi,
    private val ytsParser: YTSParser,
    private val repository: MainDao,
    private val updateUtils: UpdateUtils
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = javaClass.simpleName
    private val preference by appContext.defaultPreference()

    override suspend fun doWork(): Result {

        checkForUpdates()

        if (preference.getBoolean(GeneralSettingsFragment.FEATURED_MOVIE_NOTIFY_PREF, true))
            fetchFeaturedMovies()

        return Result.success()
    }

    private suspend fun checkForUpdates() {
        try {
            val updatePair = updateUtils.checkAsync()
            if (updatePair.second) {
                Notifications.sendUpdateNotification(applicationContext, updatePair.first)
            } else Log.e(TAG, appContext.getString(R.string.no_updates))
        } catch (e: Exception) {
            Log.w(TAG, "Failed: ${e.message}", e)
        }
    }

    private suspend fun fetchFeaturedMovies() {
        try {
            val oldFeaturedMovies = repository.getMoviesByQuery(AppInterface.FEATURED_QUERY)

            val list = ytsParser.fetchFeaturedMovies()
            val mainModel = data_main.from(list, AppInterface.FEATURED_QUERY)

            repository.saveMovies(mainModel)

            list.forEach { movie ->
                if (oldFeaturedMovies?.movies?.any { it == movie } == false) {
                    val bannerImage: Bitmap? = if (movie.imdbCode != null) {
                        val bannerUrl = tmdbApi.getMovie(movie.imdbCode).getBannerImage()
                        AppUtils.getBitmapFromUrl(bannerUrl)
                    } else null

                    Notifications.sendMovieNotification(
                        context = applicationContext,
                        movie = movie,
                        posterImage = AppUtils.getBitmapFromUrl(movie.bannerUrl),
                        bannerImage = bannerImage
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed: ${e.message}", e)
        }
    }

    companion object {
        private const val APP_WORKER_ID = "moviesy_app_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request =
                PeriodicWorkRequestBuilder<AppWorker>(20, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                APP_WORKER_ID,
                ExistingPeriodicWorkPolicy.REPLACE, request
            )
        }
    }
}