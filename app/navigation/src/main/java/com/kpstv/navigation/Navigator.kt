package com.kpstv.navigation

import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import kotlin.reflect.KClass

typealias FragClazz = KClass<out Fragment>
class Navigator(private val fm: FragmentManager, private val containerView: FrameLayout) {

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

    private val navigatorTransitionManager = NavigatorCircularTransform(fm, containerView)

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
        if (popUpToThis && getBackStackCount() > 0) {
            fm.popBackStack(getCurrentFragment()?.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        fm.commit {
            if (popUpToThis) {
                fm.fragments.forEach { remove(it) }
            }
            if (transition == TransitionType.FADE)
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            if (transition == TransitionType.SLIDE)
                setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
            when(type) {
                TransactionType.REPLACE -> replace(containerView.id, clazz.java, bundle, tagName)
                TransactionType.ADD -> add(containerView.id, clazz.java, bundle, tagName)
            }
            if (addToBackStack) addToBackStack(tagName)
        }
    }

    fun canGoBack() : Boolean {
        val count = getBackStackCount()
        if (count == 0) {
            val fragment = getCurrentFragment() ?: return false
            if (fragment is NavigatorTransmitter) {
                return fragment.getNavigator().canGoBack()
            } else {
                return false
            }
        } else {
            return true
        }
    }

    /**
     * Determines if "it went back" aka any fragment from tbe backStack
     * has been removed or not.
     * @return True if the current fragment is removed from the backStack.
     */
    fun goBack(): Boolean {
        val clazz = primaryFragClass
        if (clazz != null && !hasPrimaryFragment) {
            hasPrimaryFragment = fm.fragments.any { it::class.simpleName == primaryFragClass?.simpleName }
        }
        if (!canGoBack() && clazz != null && !hasPrimaryFragment) {
            // Create primary fragment
            navigateTo(NavOptions(clazz, transition = TransitionType.FADE))
            return false
        }
        val currentFragment = getCurrentFragment()
        val shouldPopStack = if (currentFragment is KeyedFragment) {
            !currentFragment.onBackPressed()
           /* if (fm.backStackEntryCount == 1 && currentFragment::class.simpleName == primaryFragClass?.simpleName)
                false // last primary fragment indicates activity to destroy
            else if (handle)
                return true // handled means returning true from [KeyedFragment.onBackPressed]
            else
                false*/
        } else {
            true
        }
        if (shouldPopStack) fm.popBackStackImmediate()
       /* if (currentFragment?.view != null) {
            containerView.removeView(currentFragment.view)
        }*/
        return shouldPopStack
    }

    /**
     * Returns the current fragment class.
     */
    fun getCurrentFragmentClass(): FragClazz? = fm.findFragmentById(containerView.id)?.let { it::class }

    private fun getBackStackCount() : Int = fm.backStackEntryCount

    private fun getCurrentFragment() = fm.findFragmentById(containerView.id)

    private fun getFragmentTagName(clazz: FragClazz): String = clazz.java.simpleName + "_base"

    enum class TransactionType {
        REPLACE,
        ADD
    }

    enum class TransitionType {
        NONE,
        FADE,
        SLIDE,
        CIRCULAR
    }
}