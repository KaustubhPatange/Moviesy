package com.kpstv.after

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.after.After.showTime
import com.kpstv.after.After.typeface
import com.kpstv.after.databinding.CustomToastBinding
import com.kpstv.after.utils.SimpleCallback
import com.kpstv.common_moviesy.extensions.drawableFrom
import com.kpstv.common_moviesy.extensions.hide
import java.util.concurrent.TimeUnit

class AfterRequests(
    private val time: Long,
    private val unit: TimeUnit
) {
    private val handler = Handler()

    fun perform(block: () -> Unit): AfterRequests {
        handler.postDelayed({
            try {
                block.invoke()
            } catch (e: Exception) {
                // chances of memory leak
                e.printStackTrace()
            }
        }, getTimeMilliseconds(time, unit))
        return this
    }

    fun prompt(
        context: Context,
        message: String,
        options: After.Options = After.Options(),
        doOnClose: SimpleCallback? = null
    ): AfterRequests {
        handler.postDelayed({
            displayCustomToast(context, message, options, doOnClose)
        }, getTimeMilliseconds(time, unit))
        return this
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun displayCustomToast(
        context: Context,
        message: String,
        options: After.Options = After.Options(),
        doOnClose: SimpleCallback? = null
    ) {
        try {
            if (context is AppCompatActivity) {
                if (context.isFinishing)
                    return
            }

            val binding = CustomToastBinding.inflate(LayoutInflater.from(context))
            if (typeface != null) binding.tvMessage.typeface = typeface

            binding.tvMessage.text = message

            if (!options.showIcon) binding.imageView.hide()


            /** Set image */
            setEmoji(binding, options)


            /** Create & display toast with required options */
            createToast(binding, options).show()


            /** Start the progress animation */
            createAnimator(binding, doOnClose).start()
        } catch (e: Exception) {
            // chances of memory leak
            Log.e(javaClass.simpleName, "Failed: ${e.message}", e)
        }
    }

    private fun setEmoji(binding: CustomToastBinding, options: After.Options) {
        when (options.emoji) {
            After.Emoji.SAD -> binding.imageView.setImageDrawable(
                binding.root.context.drawableFrom(
                    R.drawable.ic_sad
                )
            )
            After.Emoji.HAPPY -> binding.imageView.setImageDrawable(
                binding.root.context.drawableFrom(
                    R.drawable.ic_happy
                )
            )
        }
    }

    private fun createToast(binding: CustomToastBinding, options: After.Options): Toast {
        val toast = Toast(binding.root.context).apply {
            duration = Toast.LENGTH_LONG
            view = binding.root
        }

        when (options.displayLocation) {
            After.Location.TOP -> toast.setGravity(Gravity.TOP, 0, 70)
            After.Location.CENTER -> toast.setGravity(Gravity.TOP, 0, 0)
            After.Location.BOTTOM -> { /* Let's keep it default */ }
        }

        return toast
    }

    private fun createAnimator(
        binding: CustomToastBinding,
        doOnClose: SimpleCallback? = null
    ): ObjectAnimator {
        val animation: ObjectAnimator =
            ObjectAnimator.ofInt(binding.progressBar, "progress", binding.progressBar.progress, 0)
        animation.duration = showTime
        animation.interpolator = DecelerateInterpolator()

        animation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                doOnClose?.invoke()
            }

            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        return animation
    }

    private fun getTimeMilliseconds(time: Long, unit: TimeUnit): Long {
        return TimeUnit.MILLISECONDS.convert(time, unit)
    }
}