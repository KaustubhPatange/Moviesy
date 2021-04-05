package com.kpstv.yts.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.navigation.BaseArgs
import com.kpstv.yts.ui.navigation.Navigator

class StartViewModel : ViewModel() {
    private val _navigation = MutableLiveData<Navigator.NavOptions?>(null)
    val navigation: LiveData<Navigator.NavOptions?> = _navigation

    private val _errors = MutableLiveData<Exception?>(null)
    val errors: LiveData<Exception?> = _errors

    fun navigateTo(
        screen: StartActivity.Screen,
        args: BaseArgs? = null,
        transition: Navigator.TransitionType = Navigator.TransitionType.NONE,
        addToBackStack: Boolean = false,
        popUpTo: Boolean = false
    ) {
        _navigation.value = Navigator.NavOptions(
            clazz = screen.clazz,
            args = args,
            transition = transition,
            addToBackStack = addToBackStack,
            popUpToThis = popUpTo
        )
    }

    fun propagateError(ex: Exception) {
        _errors.value = ex
    }
}