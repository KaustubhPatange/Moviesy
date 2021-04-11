package com.kpstv.navigation.internals

import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.FragClazz
import com.kpstv.navigation.Navigator
import kotlin.reflect.KFunction1

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BottomNavigationImpl(
    private val fm: FragmentManager,
    private val containerView: FrameLayout,
    private val navView: BottomNavigationView,
    private val navFragments: Map<Int, FragClazz>,
    private val selectedNavId: Int,
    private val onNavSelectionChange: KFunction1<Int, Unit>
) : CommonLifecycleCallbacks {

    private var fragments = arrayListOf<Fragment>()
    private var selectedIndex = if (selectedNavId != -1)
        getPrimarySelectionFragmentId()
    else 0
    private val selectedFragment get() = fragments[selectedIndex]

    private var topSelectionId = if (selectedNavId != -1)
        selectedNavId
    else navFragments.keys.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            fm.commitNow {
                navFragments.values.forEach { frag ->
                    val tagFragment = fm.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)?.also { fragments.add(it) }
                    if (tagFragment == null) {
                        val fragment = frag.java.getConstructor().newInstance().also { fragments.add(it) }
                        add(containerView.id, fragment, frag.simpleName + FRAGMENT_SUFFIX)
                    }
                }
            }
            fm.commitNow {
                fragments.forEach { detach(it) }
            }
        } else {
            navFragments.values.forEach { frag ->
                val fragment = fm.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)!!
                fragments.add(fragment)
            }
            selectedIndex = savedInstanceState.getInt(KEY_SELECTION_INDEX, 0)
            topSelectionId = navFragments.keys.elementAt(selectedIndex)
        }

        navView.selectedItemId = topSelectionId
        navView.setOnNavigationItemSelectedListener call@{ item ->
            return@call onSelectNavItem(item)
        }

        setFragment(selectedFragment)
    }

    private fun onSelectNavItem(item: MenuItem) : Boolean {
        val fragment = getFragmentFromId(item.itemId)!!
        if (selectedFragment === fragment) {
            if (fragment is Navigator.BottomNavigation.Callbacks && fragment.isVisible) {
                fragment.onReselected()
            }
        } else {
            setFragment(fragment)
        }
        return true
    }

    private fun setFragment(whichFragment: Fragment) {
        var transaction = fm.beginTransaction()
        fragments.forEachIndexed { index, fragment ->
            if (fragment == whichFragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index

                if (fragment is Navigator.BottomNavigation.Callbacks) {
                    fragment.onSelected()
                }
            } else {
                transaction = transaction.detach(fragment)
            }
        }
        transaction.commit()

        onNavSelectionChange.invoke(getSelectedNavFragmentId())
    }

    private fun getSelectedNavFragmentId(): Int {
        return navFragments
            .filter { it.value.qualifiedName == selectedFragment.javaClass.name }
            .map { it.key }.first()
    }

    private fun getFragmentFromId(@IdRes id: Int): Fragment? {
        val tag = navFragments[id]!!.java.simpleName + FRAGMENT_SUFFIX
        return fm.findFragmentByTag(tag)
    }

    private fun getPrimarySelectionFragmentId(): Int = navFragments.keys.indexOf(selectedNavId)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SELECTION_INDEX, selectedIndex)
    }

    companion object {
        private const val FRAGMENT_SUFFIX = "_absBottomNav"
        private const val KEY_SELECTION_INDEX = "keySelectedIndex"
    }
}