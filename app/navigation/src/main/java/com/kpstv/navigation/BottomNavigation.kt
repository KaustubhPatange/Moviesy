package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.internals.ActivityBottomNavigationLifecycle
import com.kpstv.navigation.internals.BottomNavigationImpl
import com.kpstv.navigation.internals.FragmentBottomNavigationLifecycle
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
    if (fragment is ValueFragment) fragment.bottomNavigationState else null

    val view = fragment.requireView()

    val impl = BottomNavigationImpl(
        fm = getFragmentManager(),
        containerView = getContainerView(),
        navView = view.findViewById(obj.bottomNavigationViewId),
        navFragments = obj.bottomNavigationFragments,
        selectedNavId = obj.selectedBottomNavigationId,
        onNavSelectionChange = obj::onBottomNavigationSelectionChanged
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
        navView = activity.findViewById(obj.bottomNavigationViewId),
        navFragments = obj.bottomNavigationFragments,
        selectedNavId = obj.selectedBottomNavigationId,
        onNavSelectionChange = obj::onBottomNavigationSelectionChanged
    )

    impl.onCreate(savedStateInstance)

    activity.application.registerActivityLifecycleCallbacks(
        ActivityBottomNavigationLifecycle(activity, impl)
    )
}