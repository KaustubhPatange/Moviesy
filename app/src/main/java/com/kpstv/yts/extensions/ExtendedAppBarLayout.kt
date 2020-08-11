package com.kpstv.yts.extensions

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import kotlin.math.abs


class ExtendedAppBarLayout : AppBarLayout, OnOffsetChangedListener {

    private var state: State? = null
    private var onStateChangeListener: ((State) -> Unit)? = null
    var isAppBarExpanded = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        check(
            !(layoutParams !is CoordinatorLayout.LayoutParams
                    || parent !is CoordinatorLayout)
        ) { "MyAppBarLayout must be a direct child of CoordinatorLayout." }
        addOnOffsetChangedListener(this)
    }

    override fun onOffsetChanged(
        appBarLayout: AppBarLayout,
        verticalOffset: Int
    ) {
        state = when {
            verticalOffset == 0 -> {
                if (onStateChangeListener != null && state != State.EXPANDED) {
                    onStateChangeListener?.invoke(State.EXPANDED)
                }
                isAppBarExpanded = true
                State.EXPANDED
            }
            abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                if (onStateChangeListener != null && state != State.COLLAPSED) {
                    onStateChangeListener?.invoke(State.COLLAPSED)
                }
                isAppBarExpanded = false
                State.COLLAPSED
            }
            else -> {
                if (onStateChangeListener != null && state != State.IDLE) {
                    onStateChangeListener?.invoke(State.IDLE)
                }
                State.IDLE
            }
        }
    }

    /** Set this to listen for appBar state change */
    fun setOnStateChangeListener(listener: (State) -> Unit) {
        this.onStateChangeListener = listener
    }

    enum class State {
        COLLAPSED, EXPANDED, IDLE
    }
}