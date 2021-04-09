package com.kpstv.navigation

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.internals.BottomNavigationImpl
import com.kpstv.navigation.internals.getSaveInstanceState

/**
 * Set up navigation for [BottomNavigationView] in [Fragment].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [Navigator.BottomNavigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun Navigator.install(fragment: Fragment, obj: Navigator.BottomNavigation) {
    val fragmentSavedState = fragment.getSaveInstanceState() ?:
    if (fragment is KeyedFragment) fragment.bottomNavigationState else null

    val view = fragment.requireView()

    val impl = BottomNavigationImpl(
        fm = getFragmentManager(),
        containerView = getContainerView(),
        bottomNav = view.findViewById(obj.bottomNavigationViewId),
        bn = obj
    )

    impl.onCreate(fragmentSavedState)

    fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(
        FragmentBottomNavigationLifecycle(fragment, impl), false
    )
}

/**
 * Set up navigation for [BottomNavigationView] in [FragmentActivity].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [Navigator.BottomNavigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun Navigator.install(
    activity: FragmentActivity,
    savedStateInstance: Bundle? = null,
    obj: Navigator.BottomNavigation
) {
    val impl = BottomNavigationImpl(
        fm = getFragmentManager(),
        containerView = getContainerView(),
        bottomNav = activity.findViewById(obj.bottomNavigationViewId),
        bn = obj
    )
    impl.onCreate(savedStateInstance)

    activity.application.registerActivityLifecycleCallbacks(
        ActivityBottomNavigationLifecycle(activity, impl)
    )
}

private class FragmentBottomNavigationLifecycle(
    private val fragment: Fragment,
    private val impl: BottomNavigationImpl
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        if (fragment::class == f::class) {
            impl.onSaveInstanceState(outState)
        }
        super.onFragmentSaveInstanceState(fm, f, outState)
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        if (fragment::class == f::class) {
            fm.unregisterFragmentLifecycleCallbacks(this)
            // If the view is destroyed but fragment did not then it's likely that we do not get
            // callback on SaveInstanceState in such case we will save it in the fragment bundle.
            if (f is KeyedFragment) {
                val bundle = Bundle()
                impl.onSaveInstanceState(bundle)
                f.bottomNavigationState = bundle
            }
        }
        super.onFragmentViewDestroyed(fm, f)
    }
}

private class ActivityBottomNavigationLifecycle(
    private val activity: FragmentActivity,
    private val impl: BottomNavigationImpl
) : Application.ActivityLifecycleCallbacks {
    override fun onActivitySaveInstanceState(a: Activity, outState: Bundle) {
        if (activity::class == a::class) {
            impl.onSaveInstanceState(outState)
        }
    }

    override fun onActivityDestroyed(a: Activity) {
        if (activity::class == a::class) {
            activity.application.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
}