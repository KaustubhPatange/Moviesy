package com.kpstv.yts.extensions

import android.view.animation.Animation

typealias AnimatorCallback = (animation: Animation?) -> Unit

object DelegatedAnimator : Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation?) {

    }
    override fun onAnimationEnd(animation: Animation?) {

    }
    override fun onAnimationRepeat(animation: Animation?) {

    }
}