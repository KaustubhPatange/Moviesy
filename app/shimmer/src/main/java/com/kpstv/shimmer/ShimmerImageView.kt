package com.kpstv.shimmer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.facebook.shimmer.ShimmerFrameLayout

/** **ShimmerImageView** is an extension over facebook's standard
 *  shimmer effect.
 *
 *  @author [Kaustubh Patange](https://kaustubhpatange.github.io)
 */
class ShimmerImageView : CardView {
    @DrawableRes
    private var resId = 0
    private var scaleType: ShimmerScaleType? = null
    private var resColor = 0
    private lateinit var imageView: ImageView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    constructor(context: Context) : super(context) {
        init(context)
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.ShimmerImageView, defStyleAttr, 0)
        resId = a.getResourceId(R.styleable.ShimmerImageView_setSrc, 0)
        scaleType = ShimmerScaleType.fromId(
            a.getInt(
                R.styleable.ShimmerImageView_setScaleType,
                2
            )
        )
        resColor = a.getColor(
            R.styleable.ShimmerImageView_setShimmerColor,
            ContextCompat.getColor(context, android.R.color.darker_gray)
        )
        a.recycle()
        init(context)
    }

    fun setImage(bm: Bitmap?) {
        imageView.setImageBitmap(bm)
        hideAll()
    }

    fun setImage(dr: Drawable?) {
        imageView.setImageDrawable(dr)
        hideAll()
    }

    private fun hideAll() {
        children.forEach { if (it !is ShimmerFrameLayout) it.visibility = View.GONE }
        shimmerFrameLayout.hideShimmer()
        invalidate()
    }

    fun getImageView() = imageView

    private fun init(context: Context) {
        imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        shimmerFrameLayout = ShimmerFrameLayout(context)
        shimmerFrameLayout.setBackgroundColor(resColor)
        if (resId != 0) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, resId))
            shimmerFrameLayout.hideShimmer()
        }
        when (scaleType) {
            ShimmerScaleType.CENTER -> imageView.scaleType = ImageView.ScaleType.CENTER
            ShimmerScaleType.CENTER_CROP -> imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            ShimmerScaleType.FIT_CENTER -> imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            ShimmerScaleType.FIT_XY -> imageView.scaleType = ImageView.ScaleType.FIT_XY
        }
        imageView.invalidate()
        shimmerFrameLayout.addView(imageView)
        shimmerFrameLayout.invalidate()
        addView(shimmerFrameLayout)
        this.invalidate()
    }

    private enum class ShimmerScaleType(var id: Int) {
        CENTER(0), CENTER_CROP(1), FIT_CENTER(2), FIT_XY(3);

        companion object {
            fun fromId(id: Int): ShimmerScaleType {
                for (f in values()) {
                    if (f.id == id) return f
                }
                throw IllegalArgumentException()
            }
        }

    }

    companion object {
        private const val TAG = "ShimmerImageView"
    }
}