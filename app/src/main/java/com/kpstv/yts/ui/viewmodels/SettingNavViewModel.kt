package com.kpstv.yts.ui.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.yts.ui.fragments.SettingFragment
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.NavAnimation
import com.kpstv.navigation.Navigator
import kotlin.reflect.KClass

class SettingNavViewModel : ViewModel() {
    private val _navigation = MutableLiveData<NavigationOption>()
    val navigation: LiveData<NavigationOption> = _navigation

    fun navigateTo(
        screen: SettingFragment.Screen,
        args: BaseArgs? = null,
        transactionType: Navigator.TransactionType = Navigator.TransactionType.REPLACE,
        animation: NavAnimation = AnimationDefinition.SlideInRight,
        remember: Boolean = true
    ) {
        val options = Navigator.NavOptions(
            args = args,
            animation = animation,
            transaction = transactionType,
            remember = remember
        )
        _navigation.value = NavigationOption(screen.clazz, options)
    }

    data class NavigationOption(
        val clazz: KClass<out Fragment>,
        val options: Navigator.NavOptions
    )
}