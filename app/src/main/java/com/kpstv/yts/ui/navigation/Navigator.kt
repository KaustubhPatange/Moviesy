package com.kpstv.yts.ui.navigation

import android.os.Bundle
import android.view.Window
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import kotlin.reflect.KClass

typealias FragClazz = KClass<out Fragment>
class Navigator(window: Window, private val fm: FragmentManager, @IdRes private val containerId: Int) {

    data class NavOptions(
        val clazz: FragClazz,
        val args: BaseArgs? = null,
        val type: TransactionType = TransactionType.REPLACE,
        val transition: TransitionType = TransitionType.NONE,
        val addToBackStack: Boolean = false,
        val popUpToThis: Boolean = false
    )

    private var primaryFragClass: FragClazz? = null
    private var hasPrimaryFragment: Boolean = false

    private val navigatorTransitionManager = NavigatorCircularTransform(window, fm)

    /**
     * Sets the default fragment as the host. The [FragmentManager.popBackStack] will be called recursively
     * with proper [KeyedFragment.onBackPressed] till it finds the primary fragment.
     *
     * It will first check if the fragment exists in the backStack otherwise it will create a new one.
     *
     * In short it should be the last fragment in the host so that back press will finish the activity.
     */
    fun setPrimaryFragment(clazz: FragClazz) {
        this.primaryFragClass = clazz
    }

    fun navigateTo(navOptions: NavOptions) = with(navOptions) {
        val tagName = getFragmentTagName(clazz)
        if (transition == TransitionType.CIRCULAR) {
            navigatorTransitionManager.circularTransform()
        }
        val bundle = Bundle().apply {
            if (args != null)
                putParcelable(KeyedFragment.ARGUMENTS, args)
        }
        if (popUpToThis) popUpToEnd()
        fm.commit {
            if (transition == TransitionType.FADE)
                setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
            when(type) {
                TransactionType.REPLACE -> replace(containerId, clazz.java, bundle, tagName)
                TransactionType.ADD -> add(containerId, clazz.java, bundle, tagName)
            }
            if (addToBackStack)
                addToBackStack(tagName)
        }
    }

    fun canGoBack() : Boolean = fm.backStackEntryCount > 0

    /**
     * If returned True the activity should not call super.onBackPressed as it indicates the
     * back press is consumed on the fragment level.
     */
    fun goBack(): Boolean {
        val clazz = primaryFragClass
        if (clazz != null && !hasPrimaryFragment) {
            hasPrimaryFragment = fm.fragments.any { it::class.simpleName == primaryFragClass?.simpleName }
        }
        if (!canGoBack() && clazz != null && !hasPrimaryFragment) {
            // Create primary fragment
            navigateTo(NavOptions(clazz, transition = TransitionType.FADE))
            return true
        }
        val currentFragment = getCurrentFragment()
        val handledBackPressed = if (currentFragment is KeyedFragment) {
            val handle = currentFragment.onBackPressed()
            if (fm.backStackEntryCount == 1 && currentFragment::class.simpleName == primaryFragClass?.simpleName)
                false // last primary fragment indicates activity to destroy
            else if (handle)
                return true // handled means returning true from [KeyedFragment.onBackPressed]
            else
                false
        } else {
            false
        }
        fm.popBackStack()
        return handledBackPressed
    }

    private fun popUpToEnd() {
        if (canGoBack()) {
            val count = fm.backStackEntryCount
            for(i in 0 until count) {
                fm.popBackStack()
            }
        }
    }

    private fun getCurrentFragment() = fm.findFragmentById(containerId)

    private fun getFragmentTagName(clazz: FragClazz): String = clazz.java.simpleName + "_base"

    enum class TransactionType {
        REPLACE,
        ADD
    }

    enum class TransitionType {
        NONE,
        FADE,
        CIRCULAR
    }
}