package com.kpstv.yts.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomProgressDialogBinding
import com.kpstv.yts.extensions.SimpleCallback

class ProgressDialog(context: Context): AlertDialog(context) {
    data class Builder(private val context: Context) {
        private val progressDialog = ProgressDialog(context)
        private val binding = CustomProgressDialogBinding.bind(
            LayoutInflater.from(context)
                .inflate(R.layout.custom_progress_dialog, null)
        )

        fun setTitle(value: String): Builder {
            binding.tvTitle.text = value
            return this
        }

        fun setMessage(value: String): Builder {
            binding.tvMessage.text = value
            return this
        }

        /** Set this to null to auto close the dialog */
        fun setOnCloseListener(value: SimpleCallback?): Builder {
            binding.btnClose.setOnClickListener {
                value?.invoke()
                progressDialog.dismiss()
            }
            return this
        }

        fun setCancelable(value: Boolean): Builder {
            progressDialog.setCancelable(value)
            return this
        }

        fun build(): ProgressDialog {
            progressDialog.setView(binding.root)
            return progressDialog
        }
    }
}