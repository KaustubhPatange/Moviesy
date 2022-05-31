package com.kpstv.yts.extensions

abstract class AbstractNavigationOption {
    private var consumed: Boolean = false
    fun consumed() {
        consumed = true
    }
    fun isConsumed() = consumed
}

object AbstractNavigationOptionsExtensions {
    @Suppress("UNCHECKED_CAST")
    fun AbstractNavigationOption.consume(block: () -> Unit) {
        if (!isConsumed()) {
            consumed()
            block()
        }
    }
}