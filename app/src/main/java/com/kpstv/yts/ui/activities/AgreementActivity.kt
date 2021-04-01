package com.kpstv.yts.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewAnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.view.drawToBitmap
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ActivityAgreementBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.startActivityAndFinish
import kotlin.math.hypot

class AgreementActivity : AppCompatActivity() {

    companion object {
        const val SHOW_AGREEMENT_PREF = "show_agreement_pref"
    }

    private val binding by viewBinding(ActivityAgreementBinding::inflate)
    private val appPreference by defaultPreference()

    private var isAgreeScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.btnClose.setOnClickListener { finish() }
        binding.btnAgree.applyBottomInsets()
        binding.btnClose.applyBottomInsets()

        binding.btnAgree.setOnClickListener {
            if (!isAgreeScreen) {
                circularTransformAndUpdate()
                isAgreeScreen = true
                return@setOnClickListener
            }
            val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            if (errorCode != ConnectionResult.SUCCESS) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.play_service_error) + " (Code: $errorCode).")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> moveForward() }
                    .show()
            } else {
                moveForward()
            }
        }
    }

    private fun moveForward() {
        appPreference.writeBoolean(SHOW_AGREEMENT_PREF, true)
        startActivityAndFinish(Intent(this, MainActivity::class.java))
    }

    private fun circularTransformAndUpdate() {
        val rootView = (window.decorView.rootView as FrameLayout)

        val bitmap = binding.root.drawToBitmap()
        val imageView = createEmptyImageView().apply {
            setImageBitmap(bitmap)
        }
        rootView.addView(imageView)

        binding.btnAgree.text = getString(R.string.agree)
        binding.btnAgree.setTextColor(colorFrom(R.color.red))
        binding.root.setBackgroundColor(colorFrom(R.color.red))
        binding.tvSummary.text = getString(R.string.disclaimer_text2)

        val imageView2 = createEmptyImageView()
        rootView.addView(imageView2)

        val anim = ViewAnimationUtils.createCircularReveal(
            imageView2,
            binding.root.width / 2,
            binding.root.height / 2,
            0f,
            hypot(binding.root.width.toFloat(), binding.root.height.toFloat())
        ).apply {
            addListener(
                onStart = {
                    val bitmap2 = binding.root.drawToBitmap()
                    imageView2.setImageBitmap(bitmap2)
                },
                onEnd = {
                    rootView.removeView(imageView)
                    rootView.removeView(imageView2)
                }
            )
            duration = 400
            startDelay = 100
        }

        anim.start()
    }

    private fun createEmptyImageView() : ImageView {
        return ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
    }
}