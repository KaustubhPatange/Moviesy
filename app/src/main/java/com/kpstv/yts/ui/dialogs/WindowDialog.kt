package com.kpstv.yts.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.kpstv.yts.databinding.CustomDialogLayoutBinding
import com.kpstv.yts.extensions.SimpleCallback

class WindowDialog(context: Context) : AlertDialog(context) {

    class Builder(private val context: Context) {
        private val dialog = WindowDialog(context)
        private val view = CustomDialogLayoutBinding.inflate(LayoutInflater.from(context))
        fun setCancellable(value: Boolean): Builder {
            dialog.setCancelable(value)
            return this
        }

        fun setPositiveButton(@StringRes text: Int, callback: SimpleCallback? = null): Builder {
            view.btnPositive.text = context.getString(text)
            view.btnPositive.setOnClickListener {
                callback?.invoke()
                dialog.dismiss()
            }
            return this
        }

        fun setNegativeButton(@StringRes text: Int, callback: SimpleCallback? = null): Builder {
            view.btnNegative.text = context.getString(text)
            view.btnNegative.setOnClickListener {
                callback?.invoke()
                dialog.dismiss()
            }
            return this
        }

        fun setTitle(@StringRes text: Int): Builder {
            view.title.text = context.getString(text)
            return this
        }

        fun setSubtitle(@StringRes text: Int): Builder {
            view.subtitle.text = context.getString(text)
            return this
        }

        fun setLottieRes(@RawRes res: Int): Builder {
            view.lottieView.setAnimation(res)
            return this
        }

        fun show(): WindowDialog {
            dialog.setView(view.root)
            dialog.show()
            return dialog
        }
    }
}