package com.kpstv.yts.ui.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.SharedPayload
import com.kpstv.navigation.TransitionPayload
import com.kpstv.yts.ui.fragments.DetailFragment

class StartViewModel : ViewModel() {
    private val _navigation = MutableLiveData<Navigator.NavOptions?>(null)
    val navigation: LiveData<Navigator.NavOptions?> = _navigation

    private val _errors = MutableLiveData<Exception?>(null)
    val errors: LiveData<Exception?> = _errors

    fun navigateTo(
        screen: StartActivity.Screen,
        args: BaseArgs? = null,
        transactionType: Navigator.TransactionType = Navigator.TransactionType.REPLACE,
        transition: Navigator.TransitionType = Navigator.TransitionType.NONE,
        transitionPayload: TransitionPayload? = null,
        addToBackStack: Boolean = false,
        popUpTo: Boolean = false
    ) {
        _navigation.value = Navigator.NavOptions(
            clazz = screen.clazz,
            args = args,
            type = transactionType,
            transition = transition,
            transitionPayload = transitionPayload,
            addToBackStack = addToBackStack,
            popUpToThis = popUpTo
        )
    }

    fun goToSearch(payload: SharedPayload? = null) {
        navigateTo(
            screen = StartActivity.Screen.SEARCH,
            addToBackStack = true,
            transition = if (payload != null) Navigator.TransitionType.SHARED else Navigator.TransitionType.FADE,
            transitionPayload = payload
        )
    }

    fun goToDetail(ytsId: Int? = null, tmDbId: String? = null) {
        navigateTo(
            screen = StartActivity.Screen.DETAIL,
            args = DetailFragment.Args(tmDbId = tmDbId, ytsId = ytsId),
            addToBackStack = true,
            transition = Navigator.TransitionType.FADE,
        )
    }

    fun propagateError(ex: Exception) {
        _errors.value = ex
    }
}