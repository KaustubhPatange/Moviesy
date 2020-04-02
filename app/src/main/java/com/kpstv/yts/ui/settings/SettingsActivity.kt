package com.kpstv.yts.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.kpstv.yts.R
import com.kpstv.yts.utils.CustomBottomItem
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    /*
    * override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    } General, Storage, Developer, About
    * */

    val settingsMainFragment = SettingsMainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        settingsMainFragment.listener = {
            when(it) {
                "General" -> replaceFragment(GeneralSettingsFragment())
                "Storage" -> ""
                "LookFeel" -> replaceFragment(LookSettingsFragment())
                "Developer" -> replaceFragment(DevSettingsFragment())
                "About" -> ""
            }
        }

        replaceFragment(settingsMainFragment, false)
    }

    private fun replaceFragment(fragment: Fragment, addtoBackStack: Boolean = true) {
        val fragmentTransition = supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
        if (addtoBackStack)
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

    class SettingsMainFragment : Fragment() {
        private lateinit var act: FragmentActivity
        lateinit var listener: (String) -> Unit
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            act = activity!!

            val mainLayout = LinearLayout(context)
            mainLayout.orientation = LinearLayout.VERTICAL

            // General Settings
            val general = CustomBottomItem(context!!)
            general.setUp(R.drawable.ic_slider, "General", mainLayout, true)
            general.onClickListener = {
                listener.invoke("General")
            }

            // Storage Settings
            val storage = CustomBottomItem(context!!)
            storage.setUp(R.drawable.ic_storage, "Storage", mainLayout, true)
            storage.onClickListener = {
                listener.invoke("Storage")
            }

            // Look & Feel Settings
            val lookFeel = CustomBottomItem(context!!)
            lookFeel.setUp(R.drawable.ic_look_feel, "Look & Feel", mainLayout, true)
            lookFeel.onClickListener = {
                listener.invoke("LookFeel")
            }

            // Developer Settings
            val developer = CustomBottomItem(context!!)
            developer.setUp(R.drawable.ic_developer, "Developer (untouched)", mainLayout, true)
            developer.onClickListener = {
                listener.invoke("Developer")
            }

            // About
            val about = CustomBottomItem(context!!)
            about.setUp(R.drawable.ic_about, "About", mainLayout, true)
            about.onClickListener = {
                listener.invoke("About")
            }
            return mainLayout
        }


    }
}
