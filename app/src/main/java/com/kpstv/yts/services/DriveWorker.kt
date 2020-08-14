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
import okhttp3.internal.filterList
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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
                return restoreUserData()
            }
        }
        return Result.failure()
    }

    private suspend fun restoreUserData(): Result {
        /** Get all file list */
        val files = drive.files().list()
            .setSpaces(APP_DATA_FOLDER)
            .setFields("nextPageToken, files(id, name, modifiedTime)")
            .setPageSize(10)
            .execute().files.filterList { name == APP_DATA_FILE }

        /** Check if files are not empty */
        if (files.isEmpty()) {
            return Result.failure(
                workDataOf(
                    Pair(
                        EXCEPTION,
                        applicationContext.getString(R.string.drive_restore_empty)
                    )
                )
            )
        }

        /** Grab the latest file and download it. */
        val latestFile = files[0]
        val outputStream = ByteArrayOutputStream()
        drive.files().get(latestFile.id)
            .executeMediaAndDownloadTo(outputStream)
        val outText = String(outputStream.toByteArray())

        Log.e(TAG, "Latest Content: $outText")

        /** Start restoring contents */
        favouriteRepository.importAllDataFromJSON(outText)

        return Result.success()
    }

    private suspend fun backupUserData(): File {
        val mainData = JSONObject()
        // Add Favourites data
        val favouriteRepositoryData = favouriteRepository.exportAllDataToJSON()
        mainData.put(FavouriteRepository.FAVOURITES, favouriteRepositoryData)

        // Write to file
        val cacheFile = File(applicationContext.externalCacheDir, APP_DATA_FILE)
        cacheFile.writeText(mainData.toString())
        return cacheFile
    }

    private fun uploadFile(file: File): Boolean {
        return try {
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = APP_DATA_FILE
                parents = Collections.singletonList(APP_DATA_FOLDER)
            }
            val mediaContent = FileContent("application/json", file)
            val uploadedFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            Log.e(TAG, "FileId: ${uploadedFile.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}", e)
            false
        }
    }

    private fun initDrive() {
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, setOf(DriveScopes.DRIVE_FILE)
        )

        credential.selectedAccount =
            GoogleSignIn.getLastSignedInAccount(applicationContext)?.account
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
        private const val APP_DATA_FILE = "data.json"
        private const val APP_DATA_FOLDER = "appDataFolder"

        const val EXCEPTION = "exception"
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
                ExistingWorkPolicy.KEEP, request
            )
            return request.id
        }
    }
}