package com.kpstv.yts.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.SharedPayload
import com.kpstv.navigation.TransitionPayload
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.fragments.DetailFragment
import com.kpstv.yts.ui.fragments.MoreFragment

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

    fun goToDetail(ytsId: Int? = null, tmDbId: String? = null, movieUrl: String? = null, add: Boolean = false) {
        navigateTo(
            screen = StartActivity.Screen.DETAIL,
            args = DetailFragment.Args(tmDbId = tmDbId, ytsId = ytsId, movieUrl = movieUrl),
            addToBackStack = true,
            transactionType = if (add) Navigator.TransactionType.ADD else Navigator.TransactionType.REPLACE,
            transition = Navigator.TransitionType.FADE,
        )
    }

    fun goToMore(
        titleText: String,
        queryMap: Map<String, String>,
        base: MovieBase = MovieBase.YTS,
        add: Boolean = false
    ) {
        navigateTo(
            screen = StartActivity.Screen.MORE,
            transition = Navigator.TransitionType.FADE,
            transactionType = if (add) Navigator.TransactionType.ADD else Navigator.TransactionType.REPLACE,
            addToBackStack = true,
            args = MoreFragment.Args(
                title = titleText,
                movieBaseString = base.toString(),
                keyArrayList = ArrayList(queryMap.keys),
                valueArrayList = ArrayList(queryMap.values)
            )
        )
    }

    fun propagateError(ex: Exception) {
        _errors.value = ex
    }
}