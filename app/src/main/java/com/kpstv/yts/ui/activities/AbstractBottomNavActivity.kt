package com.kpstv.yts.ui.activities

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.reflect.KClass

/**
 * This class can be implemented in any activity where you want to setup [BottomNavigationView]
 * without the need of JetPack Navigation to overcome some of its issue mainly
 * managing backstack & to gain more control over navigation.
 *
 * @Usage
 * Extend any activity with [BottomNavigationView] with this one & implement its abstract methods.
 */
abstract class AbstractBottomNavActivity : AppCompatActivity() {
    /**
     * To get notified when current (this) fragment is selected.
     */
    interface BottomNavFragmentSelection {
        fun onSelected() {}
        fun onReselected() {}
    }

    /**
     * To get notified when current selection of fragment is changed.
     */
    interface BottomNavFragmentSelectionChanged {
        fun onBottomNavFragmentSelection(@IdRes selectedId: Int)
    }

    protected abstract val bottomNavigationViewId: Int
    protected abstract val fragmentContainerId: Int
    protected abstract val bottomNavFragments: MutableMap<Int, KClass<out Fragment>>

    fun setBottomNavFragment(@IdRes id: Int) {
        setFragment(getFragmentFromBackstack(id))
    }

    fun getSelectedBottomNavFragmentId(): Int {
        return bottomNavFragments
            .filter { it.value.qualifiedName == selectedFragment.javaClass.name }
            .map { it.key }.first()
    }

    private var fragments = arrayListOf<Fragment>()
    private var selectedIndex = 0
    private val selectedFragment get() = fragments[selectedIndex]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                bottomNavFragments.values.forEach { frag ->
                    val fragment =
                        frag.java.getConstructor().newInstance().also { fragments.add(it) }
                    add(fragmentContainerId, fragment, frag.simpleName + FRAGMENT_SUFFIX)
                }
            }
        } else {
            bottomNavFragments.values.forEach { frag ->
                val fragment =
                    supportFragmentManager.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)!!
                fragments.add(fragment)
            }

            selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX, 0)
        }

        setFragment(selectedFragment)
    }

    override fun onStart() {
        super.onStart()
        // Setting it here because the view might be null before setting contentView.
        val bottomNav = findViewById<BottomNavigationView>(bottomNavigationViewId)
        bottomNav.setOnNavigationItemSelectedListener call@{ item ->
            val fragment = getFragmentFromBackstack(item.itemId)

            if (selectedFragment === fragment) {
                if (fragment is BottomNavFragmentSelection) {
                    fragment.onReselected()
                }
            } else {
                setFragment(fragment)
            }
            return@call true
        }
    }

    private fun setFragment(whichFragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction()
        fragments.forEachIndexed { index, fragment ->
            if (fragment == whichFragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index

                if (fragment is BottomNavFragmentSelection) {
                    fragment.onSelected()
                }
            } else {
                transaction = transaction.detach(fragment)
            }
        }
        transaction.commit()

        if (this is BottomNavFragmentSelectionChanged) {
            this.onBottomNavFragmentSelection(getSelectedBottomNavFragmentId())
        }
    }

    private fun getFragmentFromBackstack(@IdRes id: Int): Fragment {
        val tag = bottomNavFragments[id]!!.java.simpleName + FRAGMENT_SUFFIX
        return supportFragmentManager.findFragmentByTag(tag)
            ?: throw IllegalAccessException("The fragment could not be found in backstack")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
    }

    companion object {
        private const val FRAGMENT_SUFFIX = "_absBottomNav"
        private const val KEY_SELECTED_INDEX = "keySelectedIndex"
    }
}