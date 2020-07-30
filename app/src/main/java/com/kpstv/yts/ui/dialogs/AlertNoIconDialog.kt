package com.kpstv.yts.ui.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.R
import com.kpstv.yts.extensions.SimpleCallback
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

        if (positiveListener ==null)
            v.alertCardPositive.setOnClickListener { finish() }
        else
            v.alertCardPositive.setOnClickListener {
                positiveListener?.invoke()
                finish()
            }

        if (negativeListener !=null) {
            v.alertCancelText.text = negativeText.toUpperCase()
            v.alertCardNegative.setOnClickListener {
                negativeListener?.invoke()
                finish()
            }
        }else {
            v.alertCardNegative.visibility = View.INVISIBLE
        }

    }

    /**
     * Thanks: https://stackoverflow.com/a/9951011/10133501
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // I only care if the event is an UP action
        if (event.action == MotionEvent.ACTION_UP) {
            // create a rect for storing the window rect
            val r = Rect(0, 0, 0, 0)
            // retrieve the windows rect
            this.window.decorView.getHitRect(r)
            // check if the event position is inside the window rect
            val intersects: Boolean = r.contains(event.x.toInt(), event.y.toInt())
            // if the event is not inside then we can close the activity
            if (!intersects && !setCancelable) {
                return true
            }
        }
        return super.onTouchEvent(event)
    }


    companion object {

        var mainTitle = ""
        var mainMessage = ""
        var positiveListener: SimpleCallback? = null
        var postiveText = "Alright"

        var setCancelable = false
        lateinit var view: View

        var negativeListener: SimpleCallback? = null
        var negativeText = "CANCEL"

        class Builder(private val context: Context?) {

            init {
                positiveListener = null
                postiveText = "Alright"
                setCancelable = true
                negativeListener = null
                negativeText = "CANCEL"
                mainMessage = ""
                mainTitle = ""
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
            fun setPositiveButton(text: String, positiveListener: SimpleCallback?): Builder {
                Companion.positiveListener = positiveListener
                postiveText = text
                return this
            }
            fun setNegativeButton(text: String, negativeListener: SimpleCallback): Builder {
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
