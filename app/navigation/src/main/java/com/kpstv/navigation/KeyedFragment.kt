package com.kpstv.navigation

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.kpstv.navigation.internals.ViewStateFragment

open class KeyedFragment(@LayoutRes id: Int) : ViewStateFragment(id) {
    constructor() : this(0)

    companion object {
        const val ARGUMENTS = "keyed_args"
    }

    /**
     * Tells [Navigator] to forcefully invoke [onBackPressed] on this fragment event though
     * [Navigator.canGoBack] returns true.
     *
     * If True then [onBackPressed] will be called regardless of any behavior. It is necessary
     * that you should return False (sometime in future) when your conditions are satisfied,
     * otherwise there will be unexpected side effects.
     *
     * This API is exposed for very edge case use only. It is not designed to always use
     * with [onBackPressed]. In extreme cases when [Navigator] fails
     * to manage back press behaviors this API should be used.
     */
    open val forceBackPress = false

    /**
     * Set custom backStack name. The same name will be used for tag when creating fragment.
     */
    open val backStackName: String? = null

    fun hasKeyArgs(): Boolean {
        return arguments?.containsKey(ARGUMENTS) ?: false
    }

    /**
     * Parse the parcelable from bundle & returns it. It is best practice to check [hasKeyArgs]
     * & then proceed with this call.
     *
     * @throws NullPointerException When it does not exist.
     */
    fun <T : BaseArgs> getKeyArgs(): T {
        return arguments?.getParcelable<T>(ARGUMENTS) as T
    }

    fun goBack() {
        safeNavigator().goBack()
    }

    /**
     * If True the event has been consumed by the [KeyedFragment].
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    private fun safeNavigator(): Navigator {
        try {
            return ((requireView().parent as View).context as NavigatorTransmitter).getNavigator()
        } catch (e: Exception) {
            throw NotImplementedError("Parent does not implement NavigatorTransmitter.")
        }
    }

    internal var bottomNavigationState: Bundle? = null
}