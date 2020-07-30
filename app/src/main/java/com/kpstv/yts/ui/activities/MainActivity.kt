package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.AppInterface.Companion.animationOptions
import com.kpstv.yts.AppInterface.Companion.setAppThemeMain
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.databinding.ActivityMainBinding
import com.kpstv.yts.extensions.viewBinding
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.ui.settings.SettingsActivity
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.dkbai.tinyhttpd.nanohttpd.webserver.SimpleWebServer

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(ActivityMainBinding::inflate)

    val TAG = "MainActivity"

    val castHelper = CastHelper()

    lateinit var drawerLayout: DrawerLayout

    private lateinit var navController: NavController
    private var isDarkTheme = true

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppThemeMain(this)
        setContentView(binding.root)

        SimpleWebServer.init(this, BuildConfig.DEBUG)

        isDarkTheme = IS_DARK_THEME

        castHelper.initCastSession(this)

        drawerLayout = binding.drawerLayout
        binding.navigationView.setNavigationItemSelectedListener(this)

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

        binding.bottomNav.setOnNavigationItemSelectedListener(bottomNavListener)

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
            navController.currentDestination?.id != R.id.homeFragment -> binding.bottomNav.selectedItemId =
                R.id.homeFragment
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        stopService(Intent(this, DownloadService::class.java))

        /** Stop the HTTP server if started */
        SimpleWebServer.stopServer()
        super.onDestroy()
    }
}

