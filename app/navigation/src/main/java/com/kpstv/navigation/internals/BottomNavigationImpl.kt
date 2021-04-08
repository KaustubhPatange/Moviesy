package com.kpstv.navigation.internals

import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.CommonLifecycleCallbacks
import com.kpstv.navigation.Navigator

internal class BottomNavigationImpl(
    private val fm: FragmentManager,
    private val containerView: FrameLayout,
    private val bottomNav: BottomNavigationView,
    private val bn: Navigator.BottomNavigation
) : CommonLifecycleCallbacks {

    private var fragments = arrayListOf<Fragment>()
    private var selectedIndex = if  (bn.selectedBottomNavigationId != -1)
        getPrimarySelectionFragmentId()
    else 0
    private val selectedFragment get() = fragments[selectedIndex]

    private var topSelectionId = if (bn.selectedBottomNavigationId != -1)
        bn.selectedBottomNavigationId
    else bn.bottomNavigationFragments.keys.first()

    override fun onCreate(lifecycleScope: LifecycleCoroutineScope, savedInstanceState: Bundle?) {
       if (savedInstanceState == null) {
           fm.commit {
               bn.bottomNavigationFragments.values.forEach { frag ->
                   val tagFragment = fm.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)?.also { fragments.add(it) }
                   if (tagFragment == null) {
                       val fragment = frag.java.getConstructor().newInstance().also { fragments.add(it) }
                       add(containerView.id, fragment, frag.simpleName + FRAGMENT_SUFFIX)
                   }
               }
           }
       } else {
           bn.bottomNavigationFragments.values.forEach { frag ->
               val fragment = fm.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)!!
               fragments.add(fragment)
           }
           selectedIndex = savedInstanceState.getInt(KEY_SELECTION_INDEX, getPrimarySelectionFragmentId())
           topSelectionId = bn.bottomNavigationFragments.keys.elementAt(selectedIndex)
       }

        bottomNav.selectedItemId = topSelectionId

        bottomNav.setOnNavigationItemSelectedListener call@{ item ->
            val fragment = getFragmentFromTag(item.itemId)

            if (selectedFragment === fragment) {
                if (fragment is Navigator.BottomNavigation.Callbacks && fragment.isVisible) {
                    fragment.onReselected()
                }
            } else {
                setFragment(fragment)
            }
            return@call true
        }

        // This will let child fragment create their view
        lifecycleScope.launchWhenStarted {
            setFragment(selectedFragment)
        }
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

        bn.onBottomNavigationSelectionChanged(getSelectedBottomNavFragmentId())
    }

    private fun getSelectedBottomNavFragmentId(): Int {
        return bn.bottomNavigationFragments
            .filter { it.value.qualifiedName == selectedFragment.javaClass.name }
            .map { it.key }.first()
    }

    private fun getFragmentFromTag(@IdRes id: Int): Fragment {
        val tag = bn.bottomNavigationFragments[id]!!.java.simpleName + FRAGMENT_SUFFIX
        return fm.findFragmentByTag(tag) ?: throw IllegalAccessException("The fragment could not be found in backstack")
    }

    private fun getPrimarySelectionFragmentId() : Int = bn.bottomNavigationFragments.keys.indexOf(bn.selectedBottomNavigationId)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SELECTION_INDEX, selectedIndex)
    }

    companion object {
        private const val FRAGMENT_SUFFIX = "_absBottomNav"
        private const val KEY_SELECTION_INDEX = "keySelectedIndex"
    }
}