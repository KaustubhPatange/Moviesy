package com.kpstv.after

import android.graphics.Typeface
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
        val showIcon: Boolean = true,
        val emoji: Emoji = Emoji.SAD, // Says how I feel everyday :(
        /**
         * You can also set custom drawable instead of emoji using this option.
         */
        @DrawableRes val drawableRes: Int? = null
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
