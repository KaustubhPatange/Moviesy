package com.kpstv.yts.vpn.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.withScale
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.kpstv.yts.R
import kotlin.math.hypot

class HelperView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    attStyle: Int = 0
) : FrameLayout(context, attributeSet, attStyle) {
    companion object {
        fun showForTarget(activity: Activity, view: View, title: String, subText: String): HelperView {
            val rect = Rect()
            view.getLocalVisibleRect(rect)
            return showForTarget(activity, rect, title, subText)
        }
        fun showForTarget(activity: Activity, rect: Rect, title: String, subText: String): HelperView {
            val helperView = HelperView(activity)
            (activity.window.decorView as FrameLayout).addView(helperView)
            helperView.showForTarget(rect, title, subText)
            return helperView
        }
    }

    private val backPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        alpha = 220
    }
    private val frontPaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    private var backgroundBitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private val blankPaint = Paint()
    private val path = Path()
    private val viewRect = Rect(100, 100, 200, 200)
    private val textView = TextView(context).apply {
        textSize = 7.sp
    }
    private val button = Button(context).apply {
        backgroundTintList = ColorStateList.valueOf(Color.DKGRAY)
        text = context.getString(R.string.close)
        setOnClickListener {
            remove()
        }
    }
    private var targetTouchListener: () -> Unit = {}
    private var dismissListener: () -> Unit = {}

    private var targetTouched: Boolean = false
    private var scale = 1f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private val animator = ObjectAnimator.ofFloat(7f, 1f).apply {
        addUpdateListener {
            backPaint.alpha = (it.animatedFraction * 220).toInt()
            scale = it.animatedValue as Float
        }
        interpolator = FastOutSlowInInterpolator()
    }

    var dismissOnTouchTarget: Boolean = true
    var dismissOnTouchOutside: Boolean = false

    init {
        addView(textView)
        addView(button)
        setWillNotDraw(false)
    }

    internal fun showForTarget(rect: Rect, title: String, subText: String) {
        viewRect.set(rect)
        textView.width = measuredWidth - measuredWidth / 4
        val spannable = SpannableStringBuilder("$title\n\n$subText")
        spannable.setSpan(ForegroundColorSpan(Color.WHITE), 0, title.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable.setSpan(RelativeSizeSpan(1.2f), 0, title.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        textView.text = spannable

        invalidate()
        animator.start()
    }

    fun remove() {
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                textView.animate().setDuration(150).alpha(0f).start()
                button.animate().setDuration(150).alpha(0f).start()
            }
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                (parent as FrameLayout).removeView(this@HelperView)
                if (targetTouched) targetTouchListener.invoke()
                dismissListener.invoke()
            }
        })
        animator.reverse()
    }

    fun setOnTargetTouchListener(block: () -> Unit) {
        targetTouchListener = block
    }

    fun setOnDismissListener(block: () -> Unit) {
        dismissListener = block
    }

    private val fullPath = Path()
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val vWidth = viewRect.width().toFloat()
        val vHeight = viewRect.height().toFloat()
        path.reset()
        fullPath.reset()
        path.addCircle(
            viewRect.left + vWidth / 2, viewRect.top + vHeight / 2,
            hypot(vWidth, vHeight) + 2.dp,
            Path.Direction.CW
        )
        fullPath.addRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), Path.Direction.CW)

        if (viewRect.bottom < measuredHeight / 2) {
            textView.y = measuredHeight / 2 + viewRect.bottom / 4f + (measuredHeight / 2 - viewRect.bottom).coerceAtMost(20.dp.toInt())
        } else {
            textView.y = measuredHeight / 3f - viewRect.height() - (viewRect.bottom - measuredHeight / 2).coerceAtMost(20.dp.toInt())
        }

        textView.updatePadding(
            left = measuredWidth / 8,
            right = measuredWidth / 8
        )

        val buttonSpec = MeasureSpec.makeMeasureSpec(measuredWidth / 2, MeasureSpec.AT_MOST)
        button.measure(buttonSpec, buttonSpec)
        button.x = measuredWidth / 8f
        button.y = measuredHeight - 70.dp

        if (backgroundBitmap == null) backgroundBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        if (bitmapCanvas == null) bitmapCanvas = Canvas(backgroundBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        bitmapCanvas?.let { bitmapCanvas ->
            bitmapCanvas.drawPath(fullPath, backPaint)
            bitmapCanvas.withScale(
                x = scale,
                y = scale,
                pivotX = viewRect.left + viewRect.width() / 2f,
                pivotY = viewRect.top + viewRect.height() / 2f
            ) {
                bitmapCanvas.drawPath(path, frontPaint)
            }
        }
        backgroundBitmap?.let { canvas.drawBitmap(it, 0f, 0f, blankPaint) }
        super.onDraw(canvas)
    }

    private val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (dismissOnTouchOutside) {
                remove()
                return false
            }
            if (dismissOnTouchTarget && viewRect.contains(e.x.toInt(), e.y.toInt())) {
                targetTouched = true
                remove()
                return false
            }
            return true
        }
    })

    override fun onTouchEvent(e: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(e)
        return true
    }

    private val Int.dp
        get() = (context.resources.displayMetrics.density * this)

    private val Int.sp
        get() = (context.resources.displayMetrics.scaledDensity * this)
}