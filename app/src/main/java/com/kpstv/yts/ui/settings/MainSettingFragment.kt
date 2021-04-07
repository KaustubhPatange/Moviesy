package com.kpstv.yts.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import com.kpstv.yts.R
import com.kpstv.yts.extensions.common.CustomBottomItem
import com.kpstv.yts.ui.fragments.SettingFragment
import com.kpstv.navigation.KeyedFragment
import com.kpstv.yts.ui.viewmodels.SettingNavViewModel

class MainSettingFragment : KeyedFragment() {
    private val navViewModel by viewModels<SettingNavViewModel>(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainLayout = LinearLayout(requireContext())
        mainLayout.orientation = LinearLayout.VERTICAL

        // General Settings
        val general = CustomBottomItem(requireContext())
        general.setUp(R.drawable.ic_slider, getString(R.string.general), mainLayout, true)
        general.onClickListener = {
            navViewModel.navigateTo(SettingFragment.Screen.GENERAL)
        }

        // Storage Settings
        val storage = CustomBottomItem(requireContext())
        storage.setUp(R.drawable.ic_storage, getString(R.string.storage), mainLayout, true)
        storage.onClickListener = {
            navViewModel.navigateTo(SettingFragment.Screen.STORAGE)
        }

        // Look & Feel Settings
        val lookFeel = CustomBottomItem(requireContext())
        lookFeel.setUp(R.drawable.ic_look_feel, getString(R.string.look_feel), mainLayout, true)
        lookFeel.onClickListener = {
            navViewModel.navigateTo(SettingFragment.Screen.LOOK_FEEL)
        }

        // Account Settings
        val account = CustomBottomItem(requireContext())
        account.setUp(R.drawable.ic_account, getString(R.string.account), mainLayout, true)
        account.onClickListener = {
            navViewModel.navigateTo(SettingFragment.Screen.ACCOUNT)
        }

        // Backup Settings
        val backup =
            CustomBottomItem(requireContext())
        backup.setUp(R.drawable.ic_backup, getString(R.string.backup_restore), mainLayout, true)
        backup.onClickListener = {
            navViewModel.navigateTo(SettingFragment.Screen.BACKUP)
        }

        // Developer Settings
        val developer =
            CustomBottomItem(requireContext())
        developer.setUp(R.drawable.ic_developer, getString(R.string.developer_untouched), mainLayout, true)
        developer.onClickListener = {
            navViewModel.navigateTo(SettingFragment.Screen.DEVELOPER)
        }

        // About
        val about = CustomBottomItem(requireContext())
        about.setUp(R.drawable.ic_about, getString(R.string.about), mainLayout, true)
        about.onClickListener = {
            navViewModel.navigateTo(SettingFragment.Screen.ABOUT)
        }

        return mainLayout
    }
}