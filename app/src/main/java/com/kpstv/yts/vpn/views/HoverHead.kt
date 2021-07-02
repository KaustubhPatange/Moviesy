package com.kpstv.yts.vpn.views

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.kpstv.yts.R

class HoverHead @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    attStyle: Int = 0
) : ShapeableImageView(context, attributeSet, attStyle) {
    init {
        alpha = 0.4f
        scaleType = ScaleType.CENTER_CROP
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes((VIEW_SIZE / 2).dp)
            .build()

        if (isInEditMode) {
            setImageResource(R.mipmap.ic_launcher)
        }
    }

    companion object {
        private const val VIEW_SIZE = 45
        private const val HIDE_ANIMATION_OFFSET: Long = 1000
        private const val ANIMATION_DURATION: Long = 100
        private const val BORDER_SIZE_DP: Int = 3
    }

    var borderColor: Int
        get() = strokePaint.color
        set(value) {
            strokePaint.color = value
            invalidate()
        }

    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = BORDER_SIZE_DP.dp
        color = if (isInEditMode) Color.RED else Color.TRANSPARENT
    }
    private val strokePath = Path()

    private var transXAnimator: ViewPropertyAnimator? = null

    private val alphaShowAnimator = ObjectAnimator.ofPropertyValuesHolder(
        this, PropertyValuesHolder.ofFloat(View.ALPHA, 1f)
    ).apply {
        interpolator = DecelerateInterpolator()
        duration = ANIMATION_DURATION
    }

    private val alphaHideAnimator = ObjectAnimator.ofPropertyValuesHolder(
        this, PropertyValuesHolder.ofFloat(View.ALPHA, 0.4f)
    ).apply {
        interpolator = FastOutLinearInInterpolator()
        startDelay = HIDE_ANIMATION_OFFSET
        duration = ANIMATION_DURATION
    }

    private val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
        this, PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f)
    ).apply {
        interpolator = DecelerateInterpolator()
        duration = ANIMATION_DURATION
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val spec = MeasureSpec.makeMeasureSpec(VIEW_SIZE.dp.toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(spec, spec)
        strokePath.addCircle(measuredWidth  / 2f, measuredHeight / 2f, (measuredWidth  - BORDER_SIZE_DP.dp) / 2f, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(strokePath, strokePaint)
    }

    private val Int.dp
        get() = (context.resources.displayMetrics.density * this)

    fun show() {
        visibility = View.VISIBLE
        alphaHideAnimator.cancel()
        transXAnimator?.cancel()

        alphaShowAnimator.start()
        scaleAnimator.start()
    }

    fun hide(animate: Boolean = true) {
        transXAnimator?.cancel()
        val xBy = measuredWidth / 4f

        if (!animate) {
            alpha = 0.4f
            scaleX = 1f
            scaleY = 1f
            x += xBy
            return
        }

        alphaHideAnimator.start()
        scaleAnimator.reverse()
        transXAnimator = animate().apply {
            interpolator = FastOutSlowInInterpolator()
            xBy((if (x > measuredWidth) +1 else -1) * xBy)
            startDelay = HIDE_ANIMATION_OFFSET
            start()
        }
    }
}