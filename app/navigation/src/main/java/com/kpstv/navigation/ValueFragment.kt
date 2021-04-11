package com.kpstv.navigation

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.kpstv.navigation.internals.ViewStateFragment

/**
 * A [Fragment] that adds "value" to the [Navigator].
 *
 * A base fragment to extend from in order to use [Navigator] effectively.
 * Child fragments of the host should extend from this class, this way
 * the fragment backStack can be effectively managed & going back is as
 * easy as calling [goBack].
 *
 * In order to pass arguments extend any class from [BaseArgs] & pass it as
 * parameter to [Navigator.navigateTo] call. Use [getKeyArgs] to retrieve
 * them.
 *
 * @see onBackPressed
 * @see BaseArgs
 */
open class ValueFragment(@LayoutRes id: Int) : ViewStateFragment(id) {
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

    /**
     * Checks if the fragment has any arguments passed during [Navigator.navigateTo] call.
     */
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

    /**
     * @see Navigator.goBack
     */
    fun goBack() {
        safeNavigator().goBack()
    }

    /**
     * Override this to receive back press.
     *
     * The back press is propagated from the host to all of the child fragments. During back
     * press if [Navigator] decides to remove this fragment from the stack it will first call
     * this method to know the result. Upon True, the event has been consumed by the
     * [ValueFragment] & [Navigator] will not pop this fragment.
     *
     * It is necessary that at one point (in future) you should return False from this method
     * so that it can safely be popped out from the backStack.
     *
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