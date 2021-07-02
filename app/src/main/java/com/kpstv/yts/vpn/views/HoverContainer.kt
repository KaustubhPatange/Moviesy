package com.kpstv.yts.vpn.views

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.contains
import androidx.dynamicanimation.animation.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.abs

typealias SimpleHoverListener = () -> Unit

interface HoverContainerController {
    fun setDrawable(@DrawableRes res: Int)
    fun setHoverBorderColor(@ColorInt color: Int)
    fun setHoverBackgroundColor(@ColorInt color: Int)
    fun addOnHoverClickListener(block: SimpleHoverListener)
    fun setOnHoverDismissed(block: SimpleHoverListener)
    fun isHoverClosed(): Boolean
    fun resetHover()
    fun removeHover()
}

class HoverContainer @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    attStyle: Int = 0
) : FrameLayout(context, attributeSet, attStyle), View.OnTouchListener, HoverContainerController {

    companion object {
        fun attachToActivity(activity: Activity): HoverContainerController {
            val view = HoverContainer(activity)
            (activity.window.decorView as FrameLayout).addView(view)
            return view
        }
    }

    private var hoverHead = HoverHead(context, attributeSet, attStyle)
    private val closeView = CloseView(context, attributeSet, attStyle)
    private var moving: Boolean = false
    private val initialPoint: Point = Point(0, 0)

    private var tracker: VelocityTracker = VelocityTracker.obtain()

    private val springAnimationX = SpringAnimation(hoverHead, DynamicAnimation.X)
    private val springAnimationY = SpringAnimation(hoverHead, DynamicAnimation.Y)

    private val listeners = arrayListOf<SimpleHoverListener>()
    private var hoverDismissListener: SimpleHoverListener? = null

    private val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return dispatchClickListeners()
        }
    })

    init {
        closeView.visibility = View.INVISIBLE
        addView(hoverHead)
        addView(closeView)
        setOnTouchListener(this)
    }

    override fun setDrawable(res: Int) {
        hoverHead.setImageResource(res)
    }

    override fun setHoverBorderColor(color: Int) {
        hoverHead.borderColor = color
    }

    override fun setHoverBackgroundColor(color: Int) {
        hoverHead.background = ColorDrawable(Color.WHITE)
    }

    override fun isHoverClosed(): Boolean = !contains(hoverHead)

    override fun resetHover() {
        addView(hoverHead, 0)
    }

    override fun removeHover() {
        removeHoverHead()
    }

    override fun addOnHoverClickListener(block: SimpleHoverListener) {
        if (!listeners.contains(block)) listeners.add(block)
    }

    override fun setOnHoverDismissed(block: SimpleHoverListener) {
        hoverDismissListener = block
    }

    private fun dispatchClickListeners(): Boolean {
        if (listeners.isEmpty()) return false
        listeners.forEach { it.invoke() }
        return true
    }

    private fun dispatchHoverDismissListener() {
        hoverDismissListener?.invoke()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        springAnimationX.apply {
            setMinValue(-hoverHead.measuredWidth.toFloat())
            setMaxValue(measuredWidth.toFloat())
        }
        springAnimationY.apply {
            setMinValue(-hoverHead.measuredHeight.toFloat())
            setMaxValue(measuredHeight.toFloat())
        }

        hoverHead.x = measuredWidth.toFloat() - (hoverHead.measuredWidth / 1.2f)
        hoverHead.y = measuredHeight / 7f

        closeView.x = (measuredWidth / 2f) - (closeView.measuredWidth / 2f)
        closeView.y = measuredHeight.toFloat()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
//        Log.e("CustomBigView", "${event.x}, ${event.y} --- ${hoverHead.x}, ${hoverHead.y}")
        if (event.rawX >= hoverHead.x && event.rawX <= (hoverHead.x + hoverHead.measuredWidth)
            && event.rawY >= hoverHead.y && event.rawY <= (hoverHead.y + hoverHead.measuredHeight)
        ) {
            gestureDetector.onTouchEvent(event)
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tracker = VelocityTracker.obtain()
                    tracker.addMovement(event)
                    hoverHead.show()
                    showCloseView()
                    initialPoint.set(
                        (event.x - hoverHead.x).toInt(),
                        (event.y - hoverHead.y).toInt()
                    )
                }
                MotionEvent.ACTION_MOVE -> {
                    tracker.addMovement(event)
                    moving = true
                    moveView(event.x, event.y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!insideCloseView) {
                        tracker.computeCurrentVelocity(1000)
                        animatedSnapToSide(event.x, event.y)
                        moving = false
                        tracker.recycle()
                    } else {
                        removeHoverHead()
                        dispatchHoverDismissListener()
                    }
                }
            }
            return true
        }
        if (moving) {
            tracker.addMovement(event)
            moveView(event.x, event.y)
        }
        if (event.action == MotionEvent.ACTION_UP && insideCloseView) {
            // called when snap inside the close view is released
            removeHoverHead()
            dispatchHoverDismissListener()
        }
        return false
    }

    private fun moveView(x: Float, y: Float) {
        if (canSnapInsideCloseView(y)) {
            snapInsideCloseView()
        } else {
            insideCloseView = false
            hoverHead.x = x - initialPoint.x
            hoverHead.y = y - initialPoint.y
        }
    }

    private fun animatedSnapToSide(x: Float, y: Float) {
//        Log.e("Snap", "$x, $y --- ${tracker.xVelocity}, ${tracker.yVelocity} --- ${getMaxDistance(tracker.xVelocity, x)}, ${getMaxDistance(tracker.yVelocity, y)}")

        val xDistance = getMaxDistance(tracker.xVelocity, x)
        val yDistance = getMaxDistance(tracker.yVelocity, y)

        val finalX = if (tracker.xVelocity > 0) x + xDistance else x - xDistance

        springAnimationX.setStartVelocity(tracker.xVelocity).animateToFinalPosition(
            if (finalX >= measuredWidth / 2) (measuredWidth.toFloat() - hoverHead.measuredWidth) else 0f
        )
        if (abs(tracker.yVelocity) > 5) {
            springAnimationY.setStartVelocity(tracker.yVelocity)
                .addUpdateListener(springAnimationUpdateListener)
                .animateToFinalPosition(
                    (if (tracker.yVelocity >= 0) y + yDistance else y - yDistance)
                        .coerceIn(0f, measuredHeight.toFloat() - hoverHead.measuredHeight)
            )
        }
        springAnimationX.addEndListener(springAnimationEndListener)
    }

    private val springAnimationUpdateListener = object : DynamicAnimation.OnAnimationUpdateListener {
        override fun onAnimationUpdate(animation: DynamicAnimation<out DynamicAnimation<*>>?, value: Float, velocity: Float) {
            if (canSnapInsideCloseView(value)) {
                springAnimationY.removeUpdateListener(this)
                snapInsideCloseView()
            }
        }
    }

    private val springAnimationEndListener = object : DynamicAnimation.OnAnimationEndListener {
        override fun onAnimationEnd(animation: DynamicAnimation<out DynamicAnimation<*>>?, canceled: Boolean, value: Float, velocity: Float) {
            springAnimationX.removeEndListener(this)

            if (insideCloseView) {
                removeHoverHead()
                dispatchHoverDismissListener()
                return
            }
            hoverHead.hide()
            hideCloseView()
        }
    }

    private fun getMaxDistance(initialVelocity: Float, distance: Float): Float {
        return (initialVelocity * initialVelocity) / (2 * 50f * distance)
    }

    private var insideCloseView: Boolean = false
    private val closeViewTranslationAnimator = ObjectAnimator.ofFloat(closeView, View.Y, 0f).apply {
        interpolator = FastOutSlowInInterpolator()
        duration = 200
    }

    private fun showCloseView() {
        closeView.visibility = View.VISIBLE
        closeViewTranslationAnimator.setFloatValues(measuredHeight - measuredHeight / 6f)
        closeViewTranslationAnimator.start()
    }

    private fun hideCloseView() {
        closeViewTranslationAnimator.setFloatValues(measuredHeight.toFloat())
        closeViewTranslationAnimator.start()
    }

    private fun canSnapInsideCloseView(y: Float): Boolean = y > (measuredHeight - measuredHeight / 4f)

    private fun snapInsideCloseView() {
        insideCloseView = true
        springAnimationX.animateToFinalPosition(closeView.x)
        springAnimationY.animateToFinalPosition(closeView.y)
    }

    private fun removeHoverHead() {
        insideCloseView = false
        moving = false
//        tracker.recycle()
        removeView(hoverHead)
        hoverHead.hide(false)
        hideCloseView()
    }
}