package com.kpstv.yts.ui.helpers

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kpstv.yts.R
import kotlinx.android.synthetic.main.custom_small_tip.view.*

class CommonTipHelper {
    private lateinit var activity: Activity
    private var title = ""
    private var buttonText = ""
    private var root: ViewGroup? = null
    private var onClick: ((View) -> Unit)? = null

    fun populateView() = with(activity) {
        if (root == null) throw Exception("Root parameter is not defined")
        val insertView =
            LayoutInflater.from(this).inflate(R.layout.custom_small_tip, root)
        insertView.tip_text.text = this@CommonTipHelper.title
        insertView.tip_button.text = buttonText
        insertView.tip_button.setOnClickListener(onClick)
    }

    data class Builder(private val activity: Activity) {
        private val helper = CommonTipHelper()

        init {
            helper.activity = activity
        }

        fun setTitle(value: String): Builder {
            helper.title = value
            return this
        }

        fun setButtonText(value: String): Builder {
            helper.buttonText = value
            return this
        }

        fun setButtonClickListener(block: (View) -> Unit): Builder {
            helper.onClick = block
            return this
        }

        /**
         * Layout in which this view to insert
         */
        fun setParentLayout(root: ViewGroup): Builder {
            helper.root = root
            return this
        }

        fun build() = helper
    }
}