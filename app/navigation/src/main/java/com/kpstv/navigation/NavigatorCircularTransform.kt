package com.kpstv.navigation

import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.math.hypot

internal class NavigatorCircularTransform(
    private val fm: FragmentManager,
    private val containerView: FrameLayout
) {

    fun circularTransform() {
        if (containerView.childCount <= 0) return

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
                containerView.addView(overlayView)

                val overlayView2 = createEmptyImageView().apply {
                    setTag(R.id.fragment_container_view_tag, Fragment()) // For fragment container
                    containerView.addView(this)
                }

                val anim = ViewAnimationUtils.createCircularReveal(
                    overlayView2,
                    containerView.width / 2,
                    containerView.height / 2,
                    0f,
                    hypot(containerView.width.toFloat(), containerView.height.toFloat())
                ).apply {
                    addListener(
                        onStart = {
                            val secondBitmap = v.drawToBitmap()
                            overlayView2.setImageBitmap(secondBitmap)
                        },
                        onEnd = {
                            containerView.removeView(overlayView)
                            containerView.removeView(overlayView2)
                        }
                    )
                    duration = 400
                    startDelay = 100
                }

                anim.start()

                fm.unregisterFragmentLifecycleCallbacks(this)
            }
        }, false)
    }

    private fun createEmptyImageView() : ImageView {
        return ImageView(containerView.context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
    }
}