package com.kpstv.navigation

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import kotlin.jvm.Throws

open class KeyedFragment(@LayoutRes id: Int) : Fragment(id) {
    constructor() : this(0)

    companion object {
        const val ARGUMENTS = "keyed_args"
    }

    fun hasKeyArgs(): Boolean {
        return arguments?.containsKey(ARGUMENTS) ?: false
    }

    /**
     * Parse the parcelable from & returns it. It is best practice to check [hasKeyArgs]
     * & then proceed with this call.
     *
     * @throws NullPointerException When it does not exist.
     */
    fun<T : BaseArgs> getKeyArgs(): T {
        return arguments?.getParcelable<T>(ARGUMENTS) as T
    }

    fun goBack() {
        safeNavigator().goBack()
    }

    private fun safeNavigator(): Navigator {
        try {
            return ((requireView().parent as View).context as NavigatorTransmitter).getNavigator()
        } catch (e: Exception) {
            throw NotImplementedError("Parent does not implement NavigatorTransmitter.")
        }
    }

    /**
     * If True the event has been consumed by the [KeyedFragment].
     */
    open fun onBackPressed(): Boolean {
        return false
    }
}