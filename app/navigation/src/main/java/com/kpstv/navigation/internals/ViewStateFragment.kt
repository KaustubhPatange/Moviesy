package com.kpstv.navigation.internals

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * A fragment that notifies view state change through [onViewStateChanged] callback.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
open class ViewStateFragment(@LayoutRes id: Int) : Fragment(id) {
    constructor() : this(0)

    /**
     * Notifies when the [View] associated with this [Fragment] goes to [ViewState.BACKGROUND]
     * or comes to [ViewState.FOREGROUND].
     *
     * When you add a new [Fragment] to the container through [FragmentManager] add transaction,
     * chances are the previous [Fragment]'s view will still be present in the container,
     * in such case [onPause], [onStop], etc. are not called.
     *
     * This callback is associated with the [View] & not to the [Fragment]'s lifecycle.
     *
     * - [ViewState.FOREGROUND] means the view is created & is visible to the user. Called after
     * [onViewCreated].
     * - [ViewState.BACKGROUND] means the view is not the current visible view in the container. This
     * doesn't guarantee that [onDestroyView] will always be called after this state change.
     */
    open fun onViewStateChanged(viewState: ViewState) { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dispatchViewState(viewState = ViewState.FOREGROUND)
        parentFragmentManager.registerFragmentLifecycleCallbacks(internalViewStateCallback, false)
    }

    override fun onDestroyView() {
        dispatchViewState(viewState = ViewState.BACKGROUND)
        parentFragmentManager.unregisterFragmentLifecycleCallbacks(internalViewStateCallback)
        super.onDestroyView()
    }

    private val internalViewStateCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)
            val parent = (v.parent as? FrameLayout) ?: return
            if (f !== this@ViewStateFragment) {
                if (parent.children.last() !== view && parent.children.asIterable().secondLast() === view) {
                    dispatchViewState(viewState = ViewState.BACKGROUND)
                }
            }
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            val v = view?.parent as? FrameLayout
            if (v != null && v.children.last() === view) {
                dispatchViewState(viewState = ViewState.FOREGROUND)
            }
            super.onFragmentViewDestroyed(fm, f)
        }
    }

    private fun dispatchViewState(viewState: ViewState) {
        if (this.viewState != viewState) {
            this.viewState = viewState
            onViewStateChanged(this.viewState)
        }
    }

    private var viewState = ViewState.UNDEFINED

    enum class ViewState {
        UNDEFINED,
        /**
         * @see [onViewStateChanged]
         */
        BACKGROUND,
        /**
         * @see [onViewStateChanged]
         */
        FOREGROUND
    }
}