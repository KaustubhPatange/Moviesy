package com.kpstv.yts.ui.navigation

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

open class KeyedFragment(@LayoutRes id: Int) : Fragment(id) {
    companion object {
        const val ARGUMENTS = "keyed_args"
    }

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
     * If True the event has consumed & will not call super.onBackPressed of the
     * parent activity.
     */
    open fun onBackPressed(): Boolean {
        return false
    }
}