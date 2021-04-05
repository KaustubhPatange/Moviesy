package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.View
import com.kpstv.common_moviesy.extensions.drawableFrom
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ActivitySettingsBinding
import com.kpstv.yts.ui.navigation.FragClazz
import com.kpstv.yts.ui.navigation.KeyedFragment
import com.kpstv.yts.ui.navigation.Navigator
import com.kpstv.yts.ui.navigation.NavigatorTransmitter
import com.kpstv.yts.ui.settings.GeneralSettingsFragment
import com.kpstv.yts.ui.settings.StorageSettingFragment

class SettingFragment : KeyedFragment(R.layout.activity_settings), NavigatorTransmitter {
    private val binding by viewBinding(ActivitySettingsBinding::bind)
    private lateinit var navigator: Navigator

    override fun getNavigator(): Navigator = navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = Navigator(requireActivity().window, childFragmentManager, binding.settingsContainer.id)

        setToolbar()
    }

    private fun setToolbar() {
        binding.toolbar.navigationIcon = drawableFrom(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener { goBack() }
    }

    enum class Screen(val clazz: FragClazz) {
        GENERAL(GeneralSettingsFragment::class),
        STORAGE(StorageSettingFragment::class),

    }
}