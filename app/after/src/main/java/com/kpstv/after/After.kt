package com.kpstv.after

import android.graphics.Typeface
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import java.util.concurrent.TimeUnit

/** **After** is a library that helps you to dispatch events "after"
 *  some time.
 *
 *  @author [Kaustubh Patange](https://kaustubhpatange.github.io)
 */
object After {
    internal const val showTime: Long = 3800
    internal var typeface: Typeface? = null
    internal var textSize: Int? = null

    fun time(time: Long, unit: TimeUnit): AfterRequests {
        return AfterRequests(time, unit)
    }

    object Config {
        fun setTextSize(int: Int): Config {
            textSize = int
            return this
        }

        fun setTypeface(tf: Typeface): Config {
            typeface = tf
            return this
        }
    }

    data class Options(
        val displayLocation: Location = Location.BOTTOM,
        /**
         * Set if the icon should be visible or not.
         */
        val showIcon: Boolean = true,
        /**
         * If [showIcon] is false this will have no effect.
         */
        val emoji: Emoji = Emoji.SAD, // Says how I feel everyday :(
        /**
         * You can also set custom drawable instead of emoji using this option.
         *
         * If [showIcon] is false this will have no effect.
         */
        @DrawableRes val drawableRes: Int? = null,
        @ColorRes val textColor: Int = R.color.default_text,
        @ColorRes val backgroundColor: Int = R.color.default_background,
        @ColorRes val progressColor: Int = R.color.default_progress,
        /**
         * This will set the image tint color
         */
        @ColorRes val imageColor: Int = R.color.default_imageview
    )

    enum class Emoji {
        HAPPY,
        SAD
    }

    enum class Location {
        TOP,
        CENTER,
        BOTTOM
    }
}
