package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.AppInterface.Companion.animationOptions
import com.kpstv.yts.AppInterface.Companion.setAppThemeMain
import com.kpstv.yts.R
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.providers.MainViewModelFactory
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    KodeinAware {

    override val kodein by kodein()
    private val factory by instance<MainViewModelFactory>()

    val TAG = "MainActivity"
    lateinit var viewModel: MainViewModel
    lateinit var drawerLayout: DrawerLayout

    lateinit var navController: NavController
    var isDarkTheme = true

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppThemeMain(this)
        setContentView(R.layout.activity_main)

        isDarkTheme = IS_DARK_THEME

        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        drawerLayout = drawer_layout
        navigationView.setNavigationItemSelectedListener(this)

        navController = findNavController(R.id.nav_host_fragment)

        /** I am not using setupWithNavController options to let it automatically
         *  navigate through the bottom nav listener instead I'll be using custom
         *  navigation Listener.
         *  The reason is though till this date there is no fix solution for saving
         *  fragment state i.e supporting multiple backstack, however there are few
         *  workarounds which can be done but it makes it more complex.
         *  So I'll be waiting till google comes out with a perfect solution.
         *
         *  PS: Also there is no way to change the default transition when user clicks
         *      on bottom nav. So we use navigateTo option to override default
         *      transition methods.
         */

        bottom_nav.setOnNavigationItemSelectedListener(bottomNavListener)

    }

    private val bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener {

        if (it.itemId != navController.currentDestination?.id)
            navController.navigate(it.itemId, null, animationOptions)

        return@OnNavigationItemSelectedListener true
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if (p0.itemId == R.id.settings) {
            drawerLayout.closeDrawer(GravityCompat.START)
            val settingIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingIntent)
        } else if (p0.itemId == R.id.downloadQueue) {
            drawerLayout.closeDrawer(GravityCompat.START)
            val downloadIntent = Intent(this, DownloadActivity::class.java)
            startActivity(downloadIntent)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if (IS_DARK_THEME != isDarkTheme) {
            val previousIntent = intent
            finish()
            startActivity(previousIntent);
        }
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            navController.currentDestination?.id != R.id.homeFragment -> bottom_nav?.selectedItemId =
                R.id.homeFragment
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        stopService(Intent(this, DownloadService::class.java))
        super.onDestroy()
    }
}

