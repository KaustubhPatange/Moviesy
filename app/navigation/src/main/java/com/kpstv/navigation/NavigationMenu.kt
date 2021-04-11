package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.navigation.NavigationView
import com.kpstv.navigation.internals.ActivityBottomNavigationLifecycle
import com.kpstv.navigation.internals.CommonNavigationImpl
import com.kpstv.navigation.internals.FragmentBottomNavigationLifecycle
import com.kpstv.navigation.internals.getSaveInstanceState

/**
 * Set up navigation for [NavigationView] in [Fragment].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [Navigator.BottomNavigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun Navigator.install(fragment: Fragment, obj: Navigator.NavigationMenu) {
    val fragmentSavedState = fragment.getSaveInstanceState() ?:
    if (fragment is KeyedFragment) fragment.bottomNavigationState else null

    val view = fragment.requireView()

    val impl = CommonNavigationImpl(
        fm = getFragmentManager(),
        containerView = getContainerView(),
        navView = view.findViewById(obj.drawerNavigationViewId),
        navFragments = obj.drawerNavigationFragments,
        selectedNavId = obj.selectedNavigationItemId,
        onNavSelectionChange = obj::onNavigationDrawerSelectionChanged
    )

    impl.onCreate(fragmentSavedState)

    fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(
        FragmentBottomNavigationLifecycle(fragment, impl), false
    )
}

/**
 * Set up navigation for [NavigationView] in [Fragment].
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
    obj: Navigator.NavigationMenu
) {
    val impl = CommonNavigationImpl(
        fm = getFragmentManager(),
        containerView = getContainerView(),
        navView = activity.findViewById(obj.drawerNavigationViewId),
        navFragments = obj.drawerNavigationFragments,
        selectedNavId = obj.selectedNavigationItemId,
        onNavSelectionChange = obj::onNavigationDrawerSelectionChanged
    )

    impl.onCreate(savedStateInstance)

    activity.application.registerActivityLifecycleCallbacks(
        ActivityBottomNavigationLifecycle(activity, impl)
    )
}