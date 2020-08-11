package com.kpstv.yts.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kpstv.yts.AppInterface.Companion.ABOUT_FRAG
import com.kpstv.yts.AppInterface.Companion.ACCOUNT_FRAG
import com.kpstv.yts.AppInterface.Companion.ACTION_REPLACE_FRAG
import com.kpstv.yts.AppInterface.Companion.DEVELOPER_FRAG
import com.kpstv.yts.AppInterface.Companion.GENERAL_FRAG
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.AppInterface.Companion.LOOK_FEEL_FRAG
import com.kpstv.yts.AppInterface.Companion.STORAGE_FRAG
import com.kpstv.yts.AppInterface.Companion.setAppThemeNoAction
import com.kpstv.yts.R
import com.kpstv.yts.extensions.common.CustomBottomItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_settings.*

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val settingsMainFragment = SettingsMainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppThemeNoAction(this)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settingsMainFragment.listener = { pref, tag ->
            when (pref) {
                GENERAL_FRAG -> replaceFragment(GeneralSettingsFragment(), tag)
                STORAGE_FRAG -> replaceFragment(StorageSettingFragment(), tag)
                LOOK_FEEL_FRAG -> replaceFragment(LookSettingsFragment(::onThemeChanged), tag)
                ACCOUNT_FRAG -> replaceFragment(AccountSettingFragment(), tag)
                DEVELOPER_FRAG -> replaceFragment(DevSettingsFragment(), tag)
                ABOUT_FRAG -> replaceFragment(AboutSettingFragment(), tag)
            }
        }

        /** Setting default fragment of this layout empty container
         */
        replaceFragment(settingsMainFragment, getString(R.string.settings), false)

        /** This check from LookSettingsFragment Listener will change
         *  current fragment with lookSettingsFragment due to result
         *  of theme being changed i.e activity recreated.
         */
        if (intent.getBooleanExtra(ACTION_REPLACE_FRAG, false))
            replaceFragment(LookSettingsFragment(::onThemeChanged), getString(R.string.look_feel))
    }

    private fun replaceFragment(fragment: Fragment, tag: String, addToBackStack: Boolean = true) {
        val fragmentTransition = supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
        if (addToBackStack)
            fragmentTransition.addToBackStack(null)
        fragmentTransition.commit()

        toolbar.title = tag
    }

    private fun onThemeChanged(value: Boolean) {
        IS_DARK_THEME = value
        val previousIntent = intent
        previousIntent.putExtra(ACTION_REPLACE_FRAG, true)
        finishAndRemoveTask()
        startActivity(previousIntent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()

            toolbar.title = getString(R.string.settings)
        }else
            super.onBackPressed()
    }

    /** This fragment will hold the category names to navigate within.
     *  A listener is called by parent activity which navigates accordingly.
     */
    class SettingsMainFragment : Fragment() {
        lateinit var listener: (String, String) -> Unit
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            val mainLayout = LinearLayout(context)
            mainLayout.orientation = LinearLayout.VERTICAL

            // General Settings
            val general =
                CustomBottomItem(requireContext())
            general.setUp(R.drawable.ic_slider, getString(R.string.general), mainLayout, true)
            general.onClickListener = {
                listener.invoke(GENERAL_FRAG, getString(R.string.general))
            }

            // Storage Settings
            val storage =
                CustomBottomItem(requireContext())
            storage.setUp(R.drawable.ic_storage, getString(R.string.storage), mainLayout, true)
            storage.onClickListener = {
                listener.invoke(STORAGE_FRAG, getString(R.string.storage))
            }

            // Look & Feel Settings
            val lookFeel =
                CustomBottomItem(requireContext())
            lookFeel.setUp(R.drawable.ic_look_feel, getString(R.string.look_feel), mainLayout, true)
            lookFeel.onClickListener = {
                listener.invoke(LOOK_FEEL_FRAG, getString(R.string.look_feel))
            }

            val account =
                CustomBottomItem(requireContext())
            account.setUp(R.drawable.ic_account, getString(R.string.account), mainLayout, true)
            account.onClickListener = {
                listener.invoke(ACCOUNT_FRAG, getString(R.string.account))
            }

            // Developer Settings
            val developer =
                CustomBottomItem(requireContext())
            developer.setUp(R.drawable.ic_developer, getString(R.string.developer_untouched), mainLayout, true)
            developer.onClickListener = {
                listener.invoke(DEVELOPER_FRAG, getString(R.string.developer_untouched))
            }

            // About
            val about =
                CustomBottomItem(requireContext())
            about.setUp(R.drawable.ic_about, getString(R.string.about), mainLayout, true)
            about.onClickListener = {
                listener.invoke(ABOUT_FRAG, getString(R.string.about))
            }
            return mainLayout
        }
    }
}
