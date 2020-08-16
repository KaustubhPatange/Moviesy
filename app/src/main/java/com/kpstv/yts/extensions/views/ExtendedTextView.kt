package com.kpstv.yts.extensions.views

import android.content.Context
import android.util.AttributeSet

/** This textView will have a circular background */
class ExtendedTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val r = measuredWidth.coerceAtLeast(measuredHeight)
        setMeasuredDimension(r, r)
    }
}