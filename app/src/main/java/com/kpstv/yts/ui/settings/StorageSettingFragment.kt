package com.kpstv.yts.ui.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.gson.Gson
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings
import com.kpstv.yts.AppSettings.STORAGE_LOCATION_PREF
import com.kpstv.yts.R
import com.kpstv.yts.data.db.repository.DownloadRepository
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.Permissions
import com.kpstv.yts.extensions.utils.ContentUtils
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class StorageSettingFragment : PreferenceFragmentCompat() {
    companion object {
        private const val STORAGE_REQUEST = 102
        const val SCAN_DOWNLOADS_PREF = "scan_downloads_pref"
    }

    @Inject
    lateinit var downloadRepository: DownloadRepository

    private val TAG = javaClass.simpleName
    private var storagePreference: Preference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.storage_preference, rootKey)

        storagePreference = findPreference(STORAGE_LOCATION_PREF)
        storagePreference?.summary = AppInterface.STORAGE_LOCATION.path
        storagePreference?.setOnPreferenceClickListener {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }
                startActivityForResult(intent, STORAGE_REQUEST)
            } catch (e: ActivityNotFoundException) {
                Toasty.error(requireContext(), getString(R.string.no_action)).show()
            }
            true
        }

        findPreference<Preference>(SCAN_DOWNLOADS_PREF)?.setOnPreferenceClickListener {
            Permissions.verifyStoragePermission(this) {
                scanForDownloads()
            }
            true
        }
    }

    private fun scanForDownloads() {
        Toasty.info(requireContext(), getString(R.string.scanning)).show()
        Coroutines.io {
            try {
                val allFiles = scanForFile(AppInterface.STORAGE_LOCATION, "details.json")
                allFiles.forEach {
                    val responseDownload = Gson().fromJson(
                        it.readText(), Model.response_download::class.java
                    )
                    downloadRepository.saveDownload(responseDownload)
                }
                Coroutines.main {
                    Toasty.info(
                        requireContext(),
                        "${allFiles.size} ${getString(R.string.scan_complete)}"
                    ).show()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Failed: ${e.message}", e)
                Coroutines.main {
                    Toasty.error(requireActivity(), "Failed: ${e.message}")
                        .show()
                }
            }
        }
    }

    private fun scanForFile(file: File, fileFilter: String): List<File> {
        val list = ArrayList<File>()
        file.listFiles()?.forEach { f ->
            Log.d(TAG, "scanForFile: ${f.name}")
            if (f.name == fileFilter) {
                list.add(f)
                Log.e(TAG, "Hit file: ${f.absolutePath}")
            }
            if (f.isDirectory) list.addAll(scanForFile(f, fileFilter))
        }
        return list
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permissions.processStoragePermission(requestCode, grantResults) {
            scanForDownloads()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == STORAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val directory = ContentUtils.getFile(uri)
                if (directory == null) {
                    Toasty.error(requireContext(), getString(R.string.error_directory)).show()
                    return@also
                }
                storagePreference?.summary = directory.path
                AppInterface.STORAGE_LOCATION = directory
                AppSettings.writeSettings(requireContext())
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }
}