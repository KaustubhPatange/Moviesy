package com.kpstv.navigation

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.math.hypot

internal class NavigatorCircularTransform(
    private val fm: FragmentManager,
    private val fragmentContainer: FrameLayout
) {
    private val containerView = fragmentContainer.rootView as FrameLayout

    fun circularTransform(payload: CircularPayload) {
        if (fragmentContainer.childCount <= 0) return

        val viewBitmap = containerView.drawToBitmap()
        val overlayView = createEmptyImageView().apply {
            setImageBitmap(viewBitmap)
            setTag(R.id.fragment_container_view_tag, Fragment()) // For fragment container
        }

        fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                if (f::class != payload.forFragment) return
                fm.unregisterFragmentLifecycleCallbacks(this)

                containerView.addView(overlayView)

                val target = payload.fromTarget ?: Rect(0, 0, containerView.width, containerView.height)

                if (f.requireView().isLaidOut) {
                    performRestCircularTransform(overlayView, target)
                } else {
                    f.requireView().viewTreeObserver?.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener{
                        override fun onPreDraw(): Boolean {
                            if (f.requireView().isLaidOut) {
                                f.requireView().viewTreeObserver.removeOnPreDrawListener(this)
                                performRestCircularTransform(overlayView, target)
                            }
                            return true
                        }
                    })
                }
            }
        }, true)
    }

    private fun performRestCircularTransform(overlayView: ImageView, target: Rect) {
        val overlayView2 = createEmptyImageView().apply {
            setTag(R.id.fragment_container_view_tag, Fragment()) // For fragment container
            containerView.addView(this)
        }

        val anim = ViewAnimationUtils.createCircularReveal(
            overlayView2,
            target.centerX(),
            target.centerY(),
            0f,
            hypot(containerView.width.toFloat(), containerView.height.toFloat())
        ).apply {
            addListener(
                onStart = {
                    overlayView.visibility = View.INVISIBLE
                    val secondBitmap = containerView.drawToBitmap()
                    overlayView2.setImageBitmap(secondBitmap)
                    overlayView.visibility = View.VISIBLE
                },
                onEnd = {
                    containerView.removeView(overlayView)
                    containerView.removeView(overlayView2)
                }
            )
            duration = 400
        }

        anim.start()
    }

    private fun createEmptyImageView() : ImageView {
        return ImageView(containerView.context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
    }
}