package com.kpstv.yts.services

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.kpstv.yts.AppInterface
import com.kpstv.yts.extensions.Notifications
import com.kpstv.yts.extensions.ProgressStreamer
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.interfaces.api.AppApi
import java.io.File
import java.util.*

class UpdateWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appApi: AppApi
) : CoroutineWorker(context, workerParams) {
    private val TAG = javaClass.simpleName
    private var progressStreamer: ProgressStreamer? = null

    private val cancelRequestCode = Notifications.getRandomNumberCode()

    override suspend fun doWork(): Result {
        Log.e(TAG, "Work Started")
        val updateUrl = inputData.getString(AppInterface.UPDATE_URL)
        return if (updateUrl != null) {
            var fileName = updateUrl.substring(
                updateUrl.lastIndexOf("/") + 1,
                updateUrl.length
            )
            if (fileName.endsWith("?dl=1")) fileName = fileName.replace("?dl=1", "")
            if (!fileName.endsWith(".apk")) fileName += ".apk"
            val file = File(
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            progressStreamer = ProgressStreamer(
                onProgressChange = {
                    if (isStopped) {
                        progressStreamer?.stop()
                        Notifications.removeUpdateProgressNotification()
                        return@ProgressStreamer
                    }
                    Notifications.sendUpdateProgressNotification(
                        applicationContext,
                        it,
                        fileName,
                        cancelRequestCode
                    )
                },
                onComplete = {
                    Notifications.removeUpdateProgressNotification()
                    Notifications.sendDownloadCompleteNotification(applicationContext, file)
                }
            )
            progressStreamer?.write(
                appApi.fetchFileAsync(updateUrl),
                file
            ) // Run synchronously on a background thread

            Log.e(TAG, "Work done")
            Result.success(workDataOf(OUTPUT_FILE_PATH to file.absolutePath))
        } else
            Result.failure()
    }

    companion object {
        const val OUTPUT_FILE_PATH = "moviesy_app_update_work:output_file"
        private const val APP_UPDATE_WORK = "moviesy_app_update_work"

        fun schedule(context: Context, updateUri: String) : UUID {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateWork = OneTimeWorkRequestBuilder<UpdateWorker>()
                .setInputData(
                    workDataOf(
                        AppInterface.UPDATE_URL to updateUri
                    )
                )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(APP_UPDATE_WORK, ExistingWorkPolicy.KEEP, updateWork)

            return updateWork.id
        }

        fun stop(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(APP_UPDATE_WORK)
        }
    }
}