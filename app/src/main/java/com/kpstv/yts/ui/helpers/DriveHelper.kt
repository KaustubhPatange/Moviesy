package com.kpstv.yts.ui.helpers

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.kpstv.yts.R
import com.kpstv.yts.extensions.AccountCallback
import com.kpstv.yts.extensions.ExceptionCallback
import com.kpstv.yts.extensions.WorkManagerCallback
import com.kpstv.yts.services.DriveWorker

class DriveHelper : SignInHelper() {

    enum class Caller {
        STORE_DATA,
        RESTORE_DATA
    }

    private val TAG = javaClass.simpleName
    private lateinit var context: Context
    private var drive: Drive? = null
    private var returnToCaller: Caller? = null
    private var uploadWorkCallback: WorkManagerCallback? = null

    data class Builder(private val create: Int = 0) {
        private val driveHelper = DriveHelper()

        fun setParent(fragment: Fragment): Builder {
            driveHelper.context = fragment.requireContext()
            driveHelper.fragment = fragment
            return this
        }

        fun setOnSignInFailed(block: ExceptionCallback): Builder {
            driveHelper.onSignInFailed = block
            return this
        }

        fun build(): DriveHelper {
            return driveHelper
        }
    }

    private val init: AccountCallback = { account ->
        val credential = GoogleAccountCredential.usingOAuth2(
            context, setOf(DriveScopes.DRIVE_FILE)
        )

        credential.selectedAccount = account.account
        drive = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()

        routeToCaller()
    }

    fun handleDriveSignInResults(requestCode: Int, data: Intent?) {
        handleSignInRequest(requestCode, data)
        if (requestCode == DRIVE_ACCESS_REQUEST_CODE)
            routeToCaller()
    }

    private fun routeToCaller() {
        val callback = returnToCaller
        returnToCaller = null
        when (callback) {
            Caller.STORE_DATA -> storeAppData(uploadWorkCallback)
            Caller.RESTORE_DATA -> restoreAppData()
        }
    }

    private fun isDriveInitialized() = drive != null

    private fun signInToGoogleDrive() {
        onSignInComplete = (init)

        init()
        signIn()
    }

    private fun isPermissionNeededForGoogleDrive(): Boolean {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(fragment.requireContext()),
                Scope(DriveScopes.DRIVE_APPDATA),
                Scope(DriveScopes.DRIVE_FILE)
            )
        ) {
            GoogleSignIn.requestPermissions(
                fragment,
                DRIVE_ACCESS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(fragment.requireContext()),
                Scope(DriveScopes.DRIVE_APPDATA),
                Scope(DriveScopes.DRIVE_FILE)
            )
            return true
        }
        return false
    }

    private fun isAllAccessGranted(): Boolean {
        if (!isDriveInitialized()) {
            signInToGoogleDrive()
            return false
        }
        if (isPermissionNeededForGoogleDrive()) {
            return false
        }
        return true
    }

    fun storeAppData(uploadWorkCallback: WorkManagerCallback? = null) {
        this.uploadWorkCallback = uploadWorkCallback
        returnToCaller = Caller.STORE_DATA
        if (!isAllAccessGranted()) // Wait till all sign in flow get's completed.
            return

        val workId = DriveWorker.schedule(fragment.requireContext(), Caller.STORE_DATA)
        val liveWorkData = WorkManager.getInstance(fragment.requireContext())
            .getWorkInfoByIdLiveData(workId)
        uploadWorkCallback?.invoke(liveWorkData)

        this.uploadWorkCallback = null
    }

    fun restoreAppData() {

    }
}