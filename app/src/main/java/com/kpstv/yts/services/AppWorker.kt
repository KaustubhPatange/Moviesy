package com.kpstv.yts.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.kpstv.yts.extensions.utils.YTSFeaturedUtils
import java.util.concurrent.TimeUnit

class AppWorker (
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val TAG = javaClass.simpleName

    override suspend fun doWork(): Result {

        Log.e(TAG, "Called doWork()")
        return Result.success()
    }

    companion object {
        const val APP_WORKER_ID = "moviesy_app_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val uploadWorkRequest: WorkRequest =
                OneTimeWorkRequestBuilder<AppWorker>()
                    .build()
            val request = PeriodicWorkRequestBuilder<AppWorker>(15, TimeUnit.MINUTES, 2, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueue(uploadWorkRequest)
           /* WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                APP_WORKER_ID,
                ExistingPeriodicWorkPolicy.REPLACE, request)*/
        }
    }
}