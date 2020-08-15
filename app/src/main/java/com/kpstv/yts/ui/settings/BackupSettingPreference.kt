package com.kpstv.yts.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkInfo
import com.kpstv.yts.R
import com.kpstv.yts.services.DriveWorker
import com.kpstv.yts.ui.dialogs.ProgressDialog
import com.kpstv.yts.ui.helpers.DriveHelper
import es.dmoral.toasty.Toasty

class BackupSettingPreference : PreferenceFragmentCompat() {

    companion object {
        const val BACKUP_DRIVE_PREF = "backup_drive_pref"
        const val RESTORE_DRIVE_PREF = "restore_drive_pref"
    }

    private lateinit var driveHelper: DriveHelper
    private var dialog: ProgressDialog? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.backup_preference, rootKey)

        driveHelper = DriveHelper.Builder()
            .setParent(this)
            .setOnSignInFailed {
                dialog?.dismiss()
            }
            .build()

        findPreference<Preference>(BACKUP_DRIVE_PREF)?.setOnPreferenceClickListener {
            val dialog = ProgressDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.drive_upload_title))
                setMessage(getString(R.string.drive_upload_text))
                setCancelable(false)
                setOnCloseListener(null)
            }.build().also { this.dialog = it }
            dialog.show()
            driveHelper.storeAppData { workInfo: LiveData<WorkInfo> ->
                workInfo.observe(this, Observer {
                    /** Nested subscribers, hence a null check. They will automatically
                     *  unsubscribe when fragment get's destroyed.
                     */
                    if (it != null && it.state.isFinished) {
                        Toasty.info(requireContext(), getString(R.string.drive_backup_complete))
                            .show()
                        dialog.dismiss()
                    }
                })
            }
            true
        }

        findPreference<Preference>(RESTORE_DRIVE_PREF)?.setOnPreferenceClickListener {
            val dialog = ProgressDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.drive_restore_title))
                setMessage(getString(R.string.drive_restore_text))
                setCancelable(false)
                setOnCloseListener(null)
            }.build().also { this.dialog = it }
            dialog.show()
            driveHelper.restoreAppData { workInfo: LiveData<WorkInfo> ->
                workInfo.observe(this, Observer {
                    /** Nested subscribers, hence a null check. They will automatically
                     *  unsubscribe when fragment get's destroyed.
                     */
                    if (it != null && it.state.isFinished) {
                        val message = it.outputData.getString(DriveWorker.EXCEPTION)
                        if (message != null)
                            Toasty.error(requireContext(), message).show()
                        else Toasty.info(
                            requireContext(),
                            getString(R.string.drive_restore_complete)
                        ).show()
                        dialog.dismiss()
                    }
                })
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        driveHelper.removeAllCallbacks()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        driveHelper.handleDriveSignInResults(requestCode, data)
    }
}