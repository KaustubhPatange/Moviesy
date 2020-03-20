package com.kpstv.yts.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.kpstv.yts.R
import com.kpstv.yts.fragments.HomeFragment
import com.kpstv.yts.fragments.LibraryFragment
import com.kpstv.yts.fragments.WatchlistFragment
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.viewmodels.MainViewModel
import com.kpstv.yts.viewmodels.providers.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    KodeinAware {

    override val kodein by kodein()
    private val factory: MainViewModelFactory by instance()

    val TAG = "MainActivity"
    lateinit var viewModel: MainViewModel
    lateinit var drawerLayout: DrawerLayout

    private val homeFragment = HomeFragment()
    private val watchlistFragment = WatchlistFragment()
    private val libraryFragment = LibraryFragment()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        drawerLayout = drawer_layout
        navigationView.setNavigationItemSelectedListener(this)

        navigationView.setNavigationItemSelectedListener(this)

        bottom_nav.setOnNavigationItemSelectedListener(bottomNavListener)

        /** Set default fragment as Home Fragment */

        bottom_nav.selectedItemId = R.id.homeFragment
    }


    private val bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener {

        when (it.itemId) {
            R.id.homeFragment -> setFragment(homeFragment)
            R.id.watchFragment -> setFragment(watchlistFragment)
            R.id.libraryFragment -> setFragment(libraryFragment)
        }

        return@OnNavigationItemSelectedListener true
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if (p0.itemId == R.id.settings) {
            drawerLayout.closeDrawer(GravityCompat.START)
            // TODO: Set up a settings activity
        }
        return true
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onDestroy() {
        stopService(Intent(this, DownloadService::class.java))
        super.onDestroy()
    }
}

