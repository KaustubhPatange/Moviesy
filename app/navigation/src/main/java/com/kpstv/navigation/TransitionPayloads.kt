package com.kpstv.navigation

import android.graphics.Rect
import android.view.View

/**
 * Base class for passing transition payload data.
 */
open class TransitionPayload

data class CircularPayload(
    /**
     * This will wait for the fragment to wait for until its view is drawn.
     *
     * Default will wait for the next navigation fragment in the [Navigator.navigateTo].
     */
    val forFragment: FragClazz? = null,
    /**
     * Start from the coordinates specified by the View. Default is from Center.
     *
     * See: [View.getLocalVisibleRect] or [View.getGlobalVisibleRect]
     */
    val fromTarget: Rect? = null
) : TransitionPayload()

data class SharedPayload(
    /**
     * A map of "from [View]" of fragment A to "Id" of fragment B. This is required
     * for creating shared animations.
     */
    val elements: Map<View, Int>
) : TransitionPayload()