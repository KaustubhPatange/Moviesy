package com.kpstv.yts.services

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import java.util.*

class CastTorrentWorker @WorkerInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return Result.success()
    }

    override fun isRunInForeground() = true

    companion object {
        private const val UNIQUE_ID = "cast_torrent_worker"
        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<CastTorrentWorker>()
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_ID, ExistingWorkPolicy.REPLACE, request
            )
        }
    }
}