package com.kpstv.yts.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.kpstv.yts.R
import com.kpstv.yts.activities.MainActivity
import com.kpstv.yts.activities.SearchActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment : Fragment(), TabLayout.OnTabSelectedListener {

    private lateinit var act: MainActivity
    var chartsFragment = ChartsFragment()
    var categoryFragment = GenreFragment()
    private var v: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return v ?: inflater.inflate(R.layout.fragment_home, container, false).also { view->

            act = activity as MainActivity

            view.searchImage.setOnClickListener {
                act.drawerLayout.openDrawer(GravityCompat.START)
            }

            view.searchCard.setOnClickListener {
                val intent = Intent(act, SearchActivity::class.java)
                startActivity(intent)
            }

            view.tabLayout.addOnTabSelectedListener(this)

            setFragment(chartsFragment)

            v = view
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
       if (tab?.position == 0) {
           setFragment(chartsFragment)
       }else if (tab?.position == 1) {
           setFragment(categoryFragment)
       }
    }

    private fun setFragment(fragment: Fragment) {
        act.supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

}
