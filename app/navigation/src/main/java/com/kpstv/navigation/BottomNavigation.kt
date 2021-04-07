package com.kpstv.navigation

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope

fun Navigator.install(fragment: Fragment, obj: Navigator.BottomNavigation) {
    val fragmentSavedState = fragment.getSaveInstanceState()

    val view = fragment.requireView()

    val impl = BottomNavigationImpl(
        fm = getFragmentManager(),
        containerView = view.parent as FrameLayout,
        bottomNav = view.findViewById(obj.bottomNavigationViewId),
        bn = obj
    )
    impl.onCreate(fragment.lifecycleScope, fragmentSavedState)

    getFragmentManager().registerFragmentLifecycleCallbacks(
        FragmentBottomNavigationLifecycle(fragment, impl), true
    )
}

fun Navigator.install(activity: FragmentActivity, obj: Navigator.BottomNavigation) {
    // TODO:
}


private class FragmentBottomNavigationLifecycle(
    private val fragment: Fragment,
    private val impl: BottomNavigationImpl
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        super.onFragmentSaveInstanceState(fm, f, outState)
        if (fragment::class == f::class) {
            impl.onSaveInstanceState(outState)
        }
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        if (fragment::class == f::class) {
            fm.unregisterFragmentLifecycleCallbacks(this)
        }
        super.onFragmentViewDestroyed(fm, f)
    }
}