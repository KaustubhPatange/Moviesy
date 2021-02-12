package com.kpstv.yts.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.databinding.ActivityAgreementBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.startActivityAndFinish

class AgreementActivity : AppCompatActivity() {

    companion object {
        const val SHOW_AGREEMENT_PREF = "show_agreement_pref"
        private const val GMS_RESULT_CODE = 102
    }

    private val binding by viewBinding(ActivityAgreementBinding::inflate)
    private val appPreference by defaultPreference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.btnClose.setOnClickListener { finish() }

        binding.btnAgree.setOnClickListener {
            val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            if (errorCode != ConnectionResult.SUCCESS) {
                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, GMS_RESULT_CODE)
                dialog.show()
            } else {
                moveForward()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GMS_RESULT_CODE) {
            moveForward()
        }
    }

    private fun moveForward() {
        appPreference.writeBoolean(SHOW_AGREEMENT_PREF, true)
        startActivityAndFinish(Intent(this, MainActivity::class.java))
    }
}