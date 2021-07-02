package com.kpstv.yts.vpn.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withScale
import com.kpstv.yts.R

class CloseView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    attStyle: Int = 0
) : AppCompatImageView(context, attributeSet, attStyle) {
    companion object {
        private const val VIEW_SIZE_DP = 50
    }

    private val closeDrawable = AppCompatResources.getDrawable(context, R.drawable.close)!!

    init {
        setImageDrawable(closeDrawable)
        background = AppCompatResources.getDrawable(context, R.drawable.close_bg)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val spec = MeasureSpec.makeMeasureSpec(VIEW_SIZE_DP.dp, MeasureSpec.EXACTLY)
        super.onMeasure(spec, spec)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.withScale(
            x = 0.5f,
            y = 0.5f,
            pivotX = (measuredWidth / 2f),
            pivotY = (measuredHeight / 2f)
        ) {
            super.onDraw(canvas)
        }
    }

    private val Int.dp
        get() = (context.resources.displayMetrics.density * this).toInt()
}