package com.kpstv.yts.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings
import com.kpstv.yts.AppSettings.STORAGE_LOCATION_PREF
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.ContentUtils
import es.dmoral.toasty.Toasty

// TODO: Testing needed for storage location
class StorageSettingFragment : PreferenceFragmentCompat() {
    companion object {
        const val STORAGE_REQUEST = 102
    }

    private val TAG = javaClass.simpleName
    private var storagePreference: Preference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.storage_preference, rootKey)

        storagePreference = findPreference(STORAGE_LOCATION_PREF)
        storagePreference?.summary = AppInterface.STORAGE_LOCATION.path
        storagePreference?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            }
            startActivityForResult(intent, STORAGE_REQUEST)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == STORAGE_REQUEST  && resultCode == Activity.RESULT_OK) {
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