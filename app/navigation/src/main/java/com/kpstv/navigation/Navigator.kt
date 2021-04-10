package com.kpstv.navigation

import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.kpstv.navigation.internals.NavigatorCircularTransform
import com.kpstv.navigation.internals.doOnLaidOut
import com.kpstv.navigation.internals.prepareForSharedTransition
import kotlin.reflect.KClass

typealias FragClazz = KClass<out Fragment>
class Navigator(private val fm: FragmentManager, private val containerView: FrameLayout) {

    data class NavOptions(
        val clazz: FragClazz,
        val args: BaseArgs? = null,
        val type: TransactionType = TransactionType.REPLACE,
        val transition: TransitionType = TransitionType.NONE,
        val transitionPayload: TransitionPayload? = null,
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
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun setPrimaryFragment(clazz: FragClazz) {
        this.primaryFragClass = clazz
    }

    /**
     * A fragment transaction.
     *
     * See: [NavOptions]
     */
    fun navigateTo(navOptions: NavOptions) = with(navOptions) options@ {
        val newFragment = clazz.java.getConstructor().newInstance()
        val tagName = if (newFragment is KeyedFragment && newFragment.backStackName != null) {
            newFragment.backStackName
        } else getFragmentTagName(clazz)

        if (transition == TransitionType.CIRCULAR) {
            val oldPayload = transitionPayload as? CircularPayload
            val payload = CircularPayload(
                forFragment = oldPayload?.forFragment ?: clazz,
                fromTarget = oldPayload?.fromTarget
            )
            navigatorTransitionManager.circularTransform(payload)
        }
        val bundle = Bundle().apply {
            if (args != null)
                putParcelable(KeyedFragment.ARGUMENTS, args)
        }
        if (popUpToThis && getBackStackCount() > 0) {
            fm.popBackStack(getCurrentFragment()?.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        // Remove duplicate backStack entry name & add it again if exist.
        // Useful when fragment is navigating to self.
        var innerAddToBackStack = false
        for(i in 0 until fm.backStackEntryCount) {
            val record = fm.getBackStackEntryAt(i)
            if (record.name == tagName) {
                fm.popBackStack(tagName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                innerAddToBackStack = true
                break
            }
        }
        fm.commit {
            if (popUpToThis) {
                fm.fragments.forEach { remove(it) }
            }
            if (transition == TransitionType.FADE)
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            if (transition == TransitionType.SLIDE)
                setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
            if (transition == TransitionType.SHARED)
                prepareForSharedTransition(fm, this@options)

            val currentFragment = fm.findFragmentByTag(tagName)
            if (currentFragment != null && currentFragment::class != clazz) {
                // (maybe) should popUp it's childFragmentManager in this case.
                show(currentFragment)
            } else {
                newFragment.arguments = bundle
                when(type) {
                    TransactionType.REPLACE -> replace(containerView.id, newFragment, tagName)
                    TransactionType.ADD -> add(containerView.id, newFragment, tagName)
                }
            }
            if (addToBackStack || innerAddToBackStack) addToBackStack(tagName)
        }
    }

    /**
     * Determines if "it can go back" aka backStack is empty or not.
     *
     * The call is recursive through child [Fragment]s that uses [Navigator].
     * By collecting information throughput it will return True or False.
     *
     * @return True means it is safe to [goBack].
     */
    @Suppress("RedundantIf")
    fun canGoBack() : Boolean {
        val count = getBackStackCount()
        if (count == 0) {
            val fragment = getCurrentFragment() ?: return false
            if (fragment is NavigatorTransmitter) {
                return fragment.getNavigator().canGoBack()
            } else if (fragment is KeyedFragment && fragment.forceBackPress) {
                return true
            } else {
                return false
            }
        } else {
            return true
        }
     }

    /**
     * Remove the latest entry from [FragmentManager]'s backStack.
     *
     * The call is recursive to child [Fragment]s, in this way their
     * [KeyedFragment.onBackPressed] are also called which returns if the backPress
     * is consumed or not. If consumed then it means child [Fragment] don't want the
     * parent [Navigator] to go back, hence [goBack] will return false
     * & no entry will be removed from the current [FragmentManager].
     *
     * @return True if the entry is removed.
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
        if (shouldPopStack) {
            fm.popBackStackImmediate()

            // Sometimes a view is retained in container view even after removing from backStack.
           /* val index = containerView.indexOfChild(currentFragment?.view)
            if (index != -1) containerView.removeViewAt(index)*/
        }
        return shouldPopStack
    }

    /**
     * Returns the current fragment class.
     */
    fun getCurrentFragmentClass() : FragClazz? = fm.findFragmentById(containerView.id)?.let { it::class }

    internal fun getFragmentManager() : FragmentManager = fm

    internal fun getContainerView() : FrameLayout = containerView

    private fun getBackStackCount() : Int = fm.backStackEntryCount

    private fun getCurrentFragment() = fm.findFragmentById(containerView.id)

    private fun getFragmentTagName(clazz: FragClazz): String = clazz.java.simpleName + FRAGMENT_SUFFIX

    enum class TransactionType {
        REPLACE,
        ADD
    }

    enum class TransitionType {
        NONE,
        FADE,
        SLIDE,
        CIRCULAR,
        SHARED
    }

    open class BottomNavigation {
        open val bottomNavigationViewId: Int = -1
        open val bottomNavigationFragments: Map<Int, FragClazz> = mapOf()
        /**
         * Default selection will be the first Id of [bottomNavigationFragments].
         */
        open val selectedBottomNavigationId: Int = -1
        open fun onBottomNavigationSelectionChanged(@IdRes selectedId: Int) {}

        /**
         * Implement this interface on child fragments to get notified
         * about selection or re-selection.
         */
        interface Callbacks {
            fun onSelected() {}
            fun onReselected() {}
        }
    }

    open class NavigationDrawer {
        open val drawerNavigationViewId: Int = -1
        open val drawerNavigationFragments: Map<Int, FragClazz> = mapOf()
        open val selectedNavigationItemId: Int = -1
        
        open fun onNavigationDrawerSelectionChanged(@IdRes selectedId: Int) {}
    }

    companion object {
        private const val FRAGMENT_SUFFIX = "_navigator"
    }
}