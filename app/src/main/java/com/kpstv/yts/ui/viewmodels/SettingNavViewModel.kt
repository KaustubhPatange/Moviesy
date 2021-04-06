package com.kpstv.yts.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.yts.ui.fragments.SettingFragment
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.Navigator

class SettingNavViewModel : ViewModel() {
    private val _navigation = MutableLiveData<Navigator.NavOptions>(null)
    val navigation: LiveData<Navigator.NavOptions> = _navigation

    fun navigateTo(
        screen: SettingFragment.Screen,
        args: BaseArgs? = null,
        transactionType: Navigator.TransactionType = Navigator.TransactionType.REPLACE,
        transitionType: Navigator.TransitionType = Navigator.TransitionType.SLIDE,
        addToBackStack: Boolean = true
    ) {
        _navigation.value = Navigator.NavOptions(
            clazz = screen.clazz,
            args = args,
            transition = transitionType,
            type = transactionType,
            addToBackStack = addToBackStack
        )
    }
}