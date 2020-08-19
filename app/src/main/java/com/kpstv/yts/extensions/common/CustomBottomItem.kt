package com.kpstv.yts.extensions.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.kpstv.yts.R
import com.kpstv.common_moviesy.extensions.drawableFrom
import kotlinx.android.synthetic.main.custom_sheet_item.view.*

class CustomBottomItem(private val context: Context) {
    lateinit var onClickListener: (View) -> Unit

    fun setUp(@DrawableRes id: Int, text: String, parent: ViewGroup, bigItem: Boolean = false) = with(context) {
        var layout = R.layout.custom_sheet_item
        if (bigItem) layout = R.layout.custom_sheet_item2
        val v: View =
            LayoutInflater.from(this).inflate(layout, parent, false)
        v.textView.text = text
        v.imageView.setImageDrawable(drawableFrom(id))

        v.mainLayout.setOnClickListener { onClickListener.invoke(it) }
        parent.addView(v)
    }
}