package com.kpstv.yts.services

import android.content.Context
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.db.repository.MainRepository
import com.kpstv.yts.data.models.data.data_main
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.extensions.utils.UpdateUtils
import com.kpstv.yts.extensions.utils.YTSFeaturedUtils
import java.util.*
import java.util.concurrent.TimeUnit

class AppWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ytsFeaturedUtils: YTSFeaturedUtils,
    private val repository: MainRepository,
    private val updateUtils: UpdateUtils
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = javaClass.simpleName

    override suspend fun doWork(): Result {

        checkForUpdates()
        fetchFeaturedMovies()

        return Result.success()
    }

    private suspend fun checkForUpdates() {
        try {
            val updatePair = updateUtils.checkAsync()
            if (updatePair.second) {
                Notifications.sendUpdateNotification(applicationContext, updatePair.first)
            } else Log.e(TAG, "No new update available")
        } catch (e: Exception) {
            Log.w(TAG, "Failed: ${e.message}", e)
        }
    }

    private suspend fun fetchFeaturedMovies() {
        try {
            val featuredMovies = repository.getMoviesByQuery(AppInterface.FEATURED_QUERY)

            val list = ytsFeaturedUtils.fetch()
            val mainModel = data_main(
                time = AppInterface.MainDateFormatter.format(Calendar.getInstance().time).toLong(),
                movies = list,
                query = AppInterface.FEATURED_QUERY,
                isMore = false
            )

            repository.saveMovies(mainModel)

            list.forEach { movie ->
                if (featuredMovies?.movies?.contains(movie) == false)
                    Notifications.sendMovieNotification(
                        context = applicationContext,
                        movieName = movie.title,
                        movieId = movie.movieId!!
                    )
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
             val request = PeriodicWorkRequestBuilder<AppWorker>(30, TimeUnit.MINUTES, 10, TimeUnit.MINUTES)
                 .setConstraints(constraints)
                 .build()
             WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                 APP_WORKER_ID,
                 ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}