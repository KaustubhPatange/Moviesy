package com.kpstv.yts.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.kpstv.yts.R
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.activities.SearchActivity
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment : Fragment(), TabLayout.OnTabSelectedListener {

    private lateinit var mainActivity: MainActivity
    var chartsFragment = ChartsFragment()
    var categoryFragment = GenreFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity

        mainActivity.viewModel.homeView?.let {
            return it
        } ?:
        return inflater.inflate(R.layout.fragment_home, container, false).also { view->

            view.searchImage.setOnClickListener {
                mainActivity.drawerLayout.openDrawer(GravityCompat.START)
            }

            view.searchCard.setOnClickListener {
                val intent = Intent(mainActivity, SearchActivity::class.java)
                startActivity(intent)
            }

            view.tabLayout.addOnTabSelectedListener(this)

            setFragment(chartsFragment)

            mainActivity.viewModel.homeView = view
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) { }

    override fun onTabUnselected(tab: TabLayout.Tab?) { }
    
    override fun onTabSelected(tab: TabLayout.Tab?) {
       if (tab?.position == 0) {
           setFragment(chartsFragment)
       }else if (tab?.position == 1) {
           setFragment(categoryFragment)
       }
    }

    private fun setFragment(fragment: Fragment) {
        mainActivity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
