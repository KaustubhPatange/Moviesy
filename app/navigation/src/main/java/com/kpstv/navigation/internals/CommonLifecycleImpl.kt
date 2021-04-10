package com.kpstv.navigation.internals

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.kpstv.navigation.KeyedFragment

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class FragmentBottomNavigationLifecycle(
    private val fragment: Fragment,
    private val impl: CommonNavigationImpl
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

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ActivityBottomNavigationLifecycle(
    private val activity: FragmentActivity,
    private val impl: CommonNavigationImpl
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