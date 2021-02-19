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

    private lateinit var view: View

    fun setUp(@DrawableRes id: Int, text: String, parent: ViewGroup, bigItem: Boolean = false): Unit = with(context) {
        var layout = R.layout.custom_sheet_item
        if (bigItem) layout = R.layout.custom_sheet_item2
        view = LayoutInflater.from(this).inflate(layout, parent, false)
        view.textView.text = text
        view.imageView.setImageDrawable(drawableFrom(id))

        view.mainLayout.setOnClickListener { onClickListener.invoke(it) }
        parent.addView(view)
    }

    fun updateTitle(title: String) {
        view.textView.text = title
    }

    fun updateIcon(@DrawableRes id: Int) {
        view.imageView.setImageDrawable(context.drawableFrom(id))
    }

    fun setOnClick(click: (View) -> Unit) {
        onClickListener = click
    }
}