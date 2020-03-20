package com.kpstv.yts.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.R
import kotlinx.android.synthetic.main.custom_alert_buttons.view.*
import kotlinx.android.synthetic.main.custom_alert_dialog.view.*

class AlertNoIconDialog : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!IS_DARK_THEME) setTheme(R.style.DialogTheme_Light)
        processAlert()
    }

    private fun processAlert() {
        val v = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog,null)
        setContentView(v)
        v.alertTitle.text =
            mainTitle
        v.alertMessage.text =
            mainMessage
        v.alertPositiveText.text =
            postiveText

        if (setCancelable) {
            setFinishOnTouchOutside(true)
        }

        if (positiveListener ==null)
            v.alertCardPositive.setOnClickListener { finish() }
        else
            v.alertCardPositive.setOnClickListener {
                positiveListener?.onClick()
                finish()
            }

        if (negativeListener !=null) {
            v.alertCancelText.text = negativeText.toUpperCase()
            v.alertCardNegative.setOnClickListener {
                negativeListener?.onClick()
                finish()
            }
        }else {
            v.alertCardNegative.visibility = View.INVISIBLE
        }

    }
    interface DialogListener {
        fun onClick()
    }
    companion object {

        var mainTitle = ""
        var mainMessage = ""
        var positiveListener: DialogListener? = null
        var postiveText = "Alright"

        var setCancelable = false
        lateinit var view: View

        var negativeListener: DialogListener? = null
        var negativeText = "CANCEL"

        var dialog: AlertDialog?=null

        class Builder(val context: Context?) {

            init {
                positiveListener = null
                postiveText = "Alright"
                setCancelable = false
                negativeListener = null
                negativeText = "CANCEL"
                mainMessage = ""
                mainTitle = ""
                dialog = null
            }

            fun setTitle(title: String): Builder {
                mainTitle = title
                return this
            }
            fun setMessage(message: String): Builder {
                mainMessage = message
                return this
            }
            fun setView(view: View): Builder {
                Companion.view = view
                return this
            }
            fun setCancelable(value: Boolean): Builder {
                setCancelable = value
                return this
            }
            fun setPositiveButton(text: String, positiveListener: DialogListener?): Builder {
                Companion.positiveListener = positiveListener
                postiveText = text
                return this
            }
            fun setNegativeButton(text: String, negativeListener: DialogListener): Builder {
                Companion.negativeListener = negativeListener
                negativeText = text
                return this
            }
            fun show() {
                val i = Intent(context, AlertNoIconDialog::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(i)
            }
        }
    }
}
