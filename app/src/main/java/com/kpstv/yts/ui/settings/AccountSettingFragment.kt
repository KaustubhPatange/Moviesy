package com.kpstv.yts.ui.settings

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ShareCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kpstv.yts.AppInterface.Companion.PURCHASE_REGEX_PATTERN
import com.kpstv.yts.AppSettings.PURCHASE_ACCOUNT_ERROR_PREF
import com.kpstv.yts.AppSettings.SHOW_ACCOUNT_ID_PREF
import com.kpstv.yts.R
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import com.kpstv.yts.ui.helpers.SignInHelper
import es.dmoral.toasty.Toasty
import org.json.JSONObject

class AccountSettingFragment : PreferenceFragmentCompat() {

    private lateinit var signInHelper: SignInHelper
    private lateinit var uid: String

    companion object {
        private const val PERMISSION_CODE = 124
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_preference, rootKey)

        initializeSignIn()

        findPreference<Preference>(SHOW_ACCOUNT_ID_PREF)?.setOnPreferenceClickListener {
            signInHelper.signIn()
            true
        }

        findPreference<Preference>(PURCHASE_ACCOUNT_ERROR_PREF)?.setOnPreferenceClickListener {
            sendErrorEmailPayment()
            true
        }
    }

    private fun sendErrorEmailPayment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requireContext()
                .checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CODE)
            return
        }
        val pattern = PURCHASE_REGEX_PATTERN.toRegex()
        val fileList = Environment.getExternalStorageDirectory().listFiles()
            ?.filter { pattern.containsMatchIn(it.name) }
        if (fileList?.isNotEmpty() == true) {
            val firstFileJson = fileList[0].readText()
            val jsonObject = JSONObject(firstFileJson)

            ShareCompat.IntentBuilder.from(requireActivity())
                .setType("message/rfc822")
                .addEmailTo(getString(R.string.author_mail))
                .setSubject("Moviesy payment error (GPay)")
                .setText("""Purchase premium but not unlocked
                        
Order Id: ${jsonObject.getString("orderId")}
Account Key: ${jsonObject.getString("uid")}
Email: ${jsonObject.getString("email")}""".trimIndent())
                .setChooserTitle("Send")
                .intent.also { startActivity(it) }
        } else
            Toasty.error(requireContext(), "No saved purchase found").show()
    }

    private fun initializeSignIn() {
        signInHelper = SignInHelper.Builder(requireContext())
            .setParent(this)
            .setOnSignInComplete {
                val message = "Account Key: ${it.id}\nEmail: ${it.email}"
                AlertNoIconDialog.Companion.Builder(requireContext())
                    .setTitle(getString(R.string.account_info))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.copy)) {
                        (requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("account_info", message))
                        Toasty.info(requireContext(), getString(R.string.copy_clipboard)).show()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { }
                    .show()
            }
            .setOnSignInFailed { e ->
                Toasty.error(requireContext(), "Failed: ${signInHelper.parseErrorCode(e)}").show()
            }
            .build()
        signInHelper.init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        signInHelper.handleSignInRequest(requestCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty())
            sendErrorEmailPayment()
    }
}