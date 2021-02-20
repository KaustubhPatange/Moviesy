package com.kpstv.after

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kpstv.after.After.showTime
import com.kpstv.after.After.textSize
import com.kpstv.after.After.typeface
import com.kpstv.after.databinding.CustomToastBinding
import com.kpstv.after.utils.SimpleCallback
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
    ): AfterRequests = with(context) {
        handler.postDelayed({
            safeContextProcess(this) {
                displayCustomToast(this, message, options, doOnClose)
            }
        }, getTimeMilliseconds(time, unit))
        return this@AfterRequests
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

            val binding = CustomToastBinding.inflate(LayoutInflater.from(context))
            typeface?.let { binding.tvMessage.typeface = it }
            textSize?.let { binding.tvMessage.textSize = it }

            binding.tvMessage.text = message

            /** Set image related queries */
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
        if (!options.showIcon) {
            binding.imageView.visibility = View.GONE
            return
        }

        when (options.emoji) {
            After.Emoji.SAD -> binding.imageView.setImageDrawable(
                ContextCompat.getDrawable(binding.root.context, R.drawable.ic_sad)
            )
            After.Emoji.HAPPY -> binding.imageView.setImageDrawable(
                ContextCompat.getDrawable(binding.root.context, R.drawable.ic_happy)
            )
        }

        if (options.drawableRes != null) ContextCompat.getDrawable(
            binding.root.context,
            options.drawableRes
        )
    }

    private fun createToast(binding: CustomToastBinding, options: After.Options): Toast {
        binding.root.setCardBackgroundColor(
            ContextCompat.getColor(
                binding.root.context, options.backgroundColor
            )
        )
        binding.tvMessage.setTextColor(
            ContextCompat.getColor(
                binding.root.context, options.textColor
            )
        )
        binding.imageView.setColorFilter(
            ContextCompat.getColor(
                binding.root.context, options.imageColor
            )
        )
        binding.progressBar.progressTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.root.context, options.progressColor
                )
            )


        val toast = Toast(binding.root.context).apply {
            duration = Toast.LENGTH_LONG
            view = binding.root
        }

        when (options.displayLocation) {
            After.Location.TOP -> toast.setGravity(Gravity.TOP, 0, 70)
            After.Location.CENTER -> toast.setGravity(Gravity.TOP, 0, 0)
            After.Location.BOTTOM -> { /* Let's keep it default */
            }
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

    private fun safeContextProcess(context: Context, callback: SimpleCallback) {
        if (context is Activity && context.isFinishing)
            return
        callback.invoke()
    }
}