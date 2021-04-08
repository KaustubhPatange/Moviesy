package com.kpstv.navigation.internals

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.transition.TransitionInflater
import com.kpstv.navigation.FragClazz
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.R
import com.kpstv.navigation.SharedPayload

internal fun FragmentTransaction.prepareForSharedTransition(fm: FragmentManager, navOptions: Navigator.NavOptions) {
    val payload = (navOptions.transitionPayload as? SharedPayload) ?: throw IllegalArgumentException("Transition is \"SHARED\" but no payload is passed?")
    val names = ArrayList<String>()
    payload.elements.keys.forEach { view ->
        val name = SharedElementCallback.getRandomTransitionName().also { names.add(it) }
        view.transitionName = name
        addSharedElement(view, name)
    }
    fm.registerFragmentLifecycleCallbacks(
        SharedElementCallback(
            clazz = navOptions.clazz,
            payload = payload,
            names = names
        ), false)
}

internal class SharedElementCallback(
    private val clazz: FragClazz,
    private val payload: SharedPayload,
    private val names: List<String>
) : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        f.sharedElementEnterTransition = TransitionInflater.from(f.requireContext())
            .inflateTransition(R.transition.change_transform)
    }
    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
        if (f::class == clazz) {
            payload.elements.values.forEachIndexed { index, id ->
                ViewCompat.setTransitionName(v.findViewById(id), names[index])
            }
            fm.unregisterFragmentLifecycleCallbacks(this)
        }
    }

    companion object {
        internal fun getRandomTransitionName() : String {
            return java.util.UUID.randomUUID().toString()
        }
    }
}