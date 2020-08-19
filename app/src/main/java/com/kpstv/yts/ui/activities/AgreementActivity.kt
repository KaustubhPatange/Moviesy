package com.kpstv.yts.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.databinding.ActivityAgreementBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.startActivityAndFinish

class AgreementActivity : AppCompatActivity() {

    companion object {
        const val SHOW_AGREEMENT_PREF = "show_agreement_pref"
    }

    private val binding by viewBinding(ActivityAgreementBinding::inflate)
    private val appPreference by defaultPreference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.btnClose.setOnClickListener { finish() }

        binding.btnAgree.setOnClickListener {
            appPreference.writeBoolean(SHOW_AGREEMENT_PREF, true)
            startActivityAndFinish(Intent(this, MainActivity::class.java))
        }
    }
}