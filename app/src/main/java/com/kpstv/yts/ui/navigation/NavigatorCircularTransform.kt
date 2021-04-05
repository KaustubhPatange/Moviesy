package com.kpstv.yts.ui.navigation

import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.math.hypot

class NavigatorCircularTransform(window: Window, private val fm: FragmentManager) {
    private val contentView = window.decorView.rootView as FrameLayout

    fun circularTransform() {
        val viewBitmap = contentView.drawToBitmap()
        val overlayView = createEmptyImageView().apply {
            setImageBitmap(viewBitmap)
            contentView.addView(this)
        }

        fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)

                val overlayView2 = createEmptyImageView()
                contentView.addView(overlayView2)

                val anim = ViewAnimationUtils.createCircularReveal(
                    overlayView2,
                    contentView.width / 2,
                    contentView.height / 2,
                    0f,
                    hypot(contentView.width.toFloat(), contentView.height.toFloat())
                ).apply {
                    addListener(
                        onStart = {
                            val secondBitmap = v.drawToBitmap()
                            overlayView2.setImageBitmap(secondBitmap)
                        },
                        onEnd = {
                            contentView.removeView(overlayView)
                            contentView.removeView(overlayView2)
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
        return ImageView(contentView.context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
    }
}