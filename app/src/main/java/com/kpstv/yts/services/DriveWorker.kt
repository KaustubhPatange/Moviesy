package com.kpstv.yts.services

import android.content.Context
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.kpstv.yts.R
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.ui.helpers.DriveHelper
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Responsible for upload contents to app drive folder.
 */
class DriveWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val favouriteRepository: FavouriteRepository
) : CoroutineWorker(appContext, workerParams) {
    private val TAG = javaClass.simpleName
    private lateinit var drive: Drive

    override suspend fun doWork(): Result {
        initDrive()

        when (DriveHelper.Caller.valueOf(inputData.getString(WORK_TYPE)!!)) {
            DriveHelper.Caller.STORE_DATA -> {
                val cacheFile = backupUserData()
                if (uploadFile(cacheFile))
                    return Result.success()
            }
            DriveHelper.Caller.RESTORE_DATA -> {

            }
        }
        return Result.failure()
    }

    private suspend fun backupUserData(): File {
        val mainData = JSONObject()
        // Add Favourites data
        val favouriteRepositoryData = favouriteRepository.exportAllDataToJSON()
        mainData.put(FavouriteRepository.FAVOURITES, favouriteRepositoryData)

        // Write to file
        val cacheFile = File(applicationContext.externalCacheDir, "data.json")
        cacheFile.writeText(mainData.toString())
        Log.e(TAG, "CacheFile: $mainData")
        return cacheFile
    }

    private fun uploadFile(file: File): Boolean {
        return try {
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = "data.json"
                parents = Collections.singletonList("appDataFolder")
            }
            val mediaContent = FileContent("application/json", file)
            val uploadedFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            Log.e(TAG, "FileId: ${uploadedFile.id}")
            true
        }catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}", e)
            false
        }
    }

    private fun initDrive() {
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, setOf(DriveScopes.DRIVE_FILE)
        )

        credential.selectedAccount = GoogleSignIn.getLastSignedInAccount(applicationContext)?.account
        drive = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(applicationContext.getString(R.string.app_name))
            .build()
    }

    companion object {
        private const val WORKER_ID = "app_drive_worker"

        const val WORK_TYPE = "work_type"

        fun schedule(context: Context, workType: DriveHelper.Caller): UUID {
            val data = Data.Builder()
                .putString(WORK_TYPE, workType.name)
                .build()
            val request = OneTimeWorkRequestBuilder<DriveWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORKER_ID,
                ExistingWorkPolicy.REPLACE, request
            )
            return request.id
        }
    }
}