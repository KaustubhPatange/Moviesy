package com.kpstv.yts.ui.activities

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.kpstv.common_moviesy.extensions.makeFullScreen
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ActivityCrashOnBinding
import com.kpstv.yts.extensions.utils.AppUtils

class CrashOnActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private val binding by viewBinding(ActivityCrashOnBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.e(TAG, "Application crashed\n${CustomActivityOnCrash.getStackTraceFromIntent(intent)}")

        binding.btnStack.setOnClickListener {
            createCustomCrashDialog()
        }
    }

    private fun createCustomCrashDialog() {
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        textView.setPadding(20)
        textView.apply {
            isVerticalScrollBarEnabled = true
            movementMethod = ScrollingMovementMethod.getInstance()
            scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            text = CustomActivityOnCrash.getStackTraceFromIntent(intent)
        }

        AlertDialog.Builder(this)
            .setView(textView)
            .setPositiveButton(getString(R.string.close), null)
            .show()
    }
}