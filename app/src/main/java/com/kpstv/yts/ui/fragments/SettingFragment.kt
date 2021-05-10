package com.kpstv.yts.ui.fragments

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.drawableFrom
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.*
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentSettingsBinding
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.helpers.ThemeHelper.registerForThemeChange
import com.kpstv.yts.ui.settings.*
import com.kpstv.yts.ui.viewmodels.SettingNavViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import kotlinx.android.parcel.Parcelize
import kotlin.reflect.KClass

class SettingFragment : ValueFragment(R.layout.fragment_settings), NavigatorTransmitter, LookSettingsFragment.ThemeChangeCallbacks {
    private val binding by viewBinding(FragmentSettingsBinding::bind)
    private val navViewModel by activityViewModels<StartViewModel>()
    private val viewModel by viewModels<SettingNavViewModel>()
    private lateinit var navigator: Navigator

    override fun getNavigator(): Navigator = navigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerForThemeChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = Navigator.with(this, savedInstanceState).initialize(binding.settingsContainer)

        setToolbar()
        viewModel.navigation.observe(viewLifecycleOwner, navigationObserver)

        if (savedInstanceState == null) {
            navigator.navigateTo(Screen.MAIN.clazz)
        }

        if (hasKeyArgs()) {
            manageArguments()
        }
    }

    private val navigationObserver = Observer { options: SettingNavViewModel.NavigationOption? ->
        options?.let { opt ->
            navigator.navigateTo(opt.clazz, opt.options)
            binding.toolbar.title = getString(Screen.getTitle(opt.clazz))
        }
    }

    private fun setToolbar() {
        binding.toolbar.applyTopInsets()
        binding.toolbar.navigationIcon = drawableFrom(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { goBack() }
    }

    private fun manageArguments() {
        val keys = getKeyArgs<Args>()
        if (keys.openLookFeel) {
            viewModel.navigateTo(Screen.LOOK_FEEL, animation = AnimationDefinition.None)
        }
    }

    override fun onThemeChanged(viewRect: Rect) {
        navViewModel.navigateTo(
            screen = StartActivity.Screen.SETTING,
            args = Args(openLookFeel = true),
            animation = AnimationDefinition.CircularReveal(
                forFragment = LookSettingsFragment::class,
                fromTarget = viewRect
            ),
            historyOptions = HistoryOptions.SingleTopInstance
        )
    }

    override fun onBackPressed(): Boolean {
        binding.toolbar.title = getString(Screen.MAIN.title)
        if (navigator.canFinish()) {
            return super.onBackPressed()
        }
        return true
    }

    enum class Screen(val clazz: KClass<out Fragment>, @StringRes val title: Int) {
        MAIN(MainSettingFragment::class, R.string.settings),
        GENERAL(GeneralSettingsFragment::class, R.string.general),
        STORAGE(StorageSettingFragment::class, R.string.storage),
        LOOK_FEEL(LookSettingsFragment::class, R.string.look_feel),
        ACCOUNT(AccountSettingFragment::class, R.string.account),
        BACKUP(BackupSettingPreference::class, R.string.backup_restore),
        DEVELOPER(DevSettingsFragment::class, R.string.developer_untouched),
        ABOUT(AboutSettingFragment::class, R.string.about);

        companion object {
            fun getTitle(clazz: KClass<out Fragment>): Int {
                return values().firstOrNull { it.clazz == clazz }?.title ?: MAIN.title
            }
        }
    }

    @Parcelize
    data class Args(val openLookFeel: Boolean = false): BaseArgs(), Parcelable
}