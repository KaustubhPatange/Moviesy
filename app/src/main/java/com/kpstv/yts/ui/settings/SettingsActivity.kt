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
import com.kpstv.yts.AppInterface.Companion.DEVELOPER_FRAG
import com.kpstv.yts.AppInterface.Companion.GENERAL_FRAG
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.AppInterface.Companion.LOOK_FEEL_FRAG
import com.kpstv.yts.AppInterface.Companion.REPLACE_FRAG
import com.kpstv.yts.AppInterface.Companion.STORAGE_FRAG
import com.kpstv.yts.AppInterface.Companion.setAppThemeNoAction
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.CustomBottomItem
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    val settingsMainFragment = SettingsMainFragment()
    val lookSettingsFragment = LookSettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppThemeNoAction(this)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /** This listener on DARK_THEME switch button will help
         *  us to change theme of this activity by recreating
         *  it after destroying :)
         */
        lookSettingsFragment.onDarkThemeChangeListener = {
            IS_DARK_THEME = it
            val previousIntent = intent
            previousIntent.putExtra(REPLACE_FRAG, true)
            finish()
            startActivity(previousIntent)
        }

        settingsMainFragment.listener = {
            when (it) {
                GENERAL_FRAG -> replaceFragment(GeneralSettingsFragment())
                STORAGE_FRAG -> replaceFragment(StorageSettingFragment())
                LOOK_FEEL_FRAG -> replaceFragment(lookSettingsFragment)
                ACCOUNT_FRAG -> replaceFragment(AccountSettingFragment())
                DEVELOPER_FRAG -> replaceFragment(DevSettingsFragment())
                ABOUT_FRAG -> replaceFragment(AboutSettingFragment())
            }
        }

        /** Setting default fragment of this layout empty container
         */
        replaceFragment(settingsMainFragment, false)

        /** This check from LookSettingsFragment Listener will change
         *  current fragment with lookSettingsFragment due to result
         *  of theme being changed i.e activity recreated.
         */
        if (intent.getBooleanExtra(REPLACE_FRAG, false))
            replaceFragment(lookSettingsFragment)
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentTransition = supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
        if (addToBackStack)
            fragmentTransition.addToBackStack(null)
        fragmentTransition.commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return
        }
        super.onBackPressed()
    }

    /** This fragment will hold the category names to navigate within.
     *  A listener is called by parent activity which navigates accordingly.
     */
    class SettingsMainFragment : Fragment() {
        lateinit var listener: (String) -> Unit
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            val mainLayout = LinearLayout(context)
            mainLayout.orientation = LinearLayout.VERTICAL

            // General Settings
            val general = CustomBottomItem(requireContext())
            general.setUp(R.drawable.ic_slider, "General", mainLayout, true)
            general.onClickListener = {
                listener.invoke(GENERAL_FRAG)
            }

            // Storage Settings
            val storage = CustomBottomItem(requireContext())
            storage.setUp(R.drawable.ic_storage, "Storage", mainLayout, true)
            storage.onClickListener = {
                listener.invoke(STORAGE_FRAG)
            }

            // Look & Feel Settings
            val lookFeel = CustomBottomItem(requireContext())
            lookFeel.setUp(R.drawable.ic_look_feel, "Look & Feel", mainLayout, true)
            lookFeel.onClickListener = {
                listener.invoke(LOOK_FEEL_FRAG)
            }

            val account = CustomBottomItem(requireContext())
            account.setUp(R.drawable.ic_account, "Account", mainLayout, true)
            account.onClickListener = {
                listener.invoke(ACCOUNT_FRAG)
            }

            // Developer Settings
            val developer = CustomBottomItem(requireContext())
            developer.setUp(R.drawable.ic_developer, "Developer (untouched)", mainLayout, true)
            developer.onClickListener = {
                listener.invoke(DEVELOPER_FRAG)
            }

            // About
            val about = CustomBottomItem(requireContext())
            about.setUp(R.drawable.ic_about, "About", mainLayout, true)
            about.onClickListener = {
                listener.invoke(ABOUT_FRAG)
            }
            return mainLayout
        }
    }
}
