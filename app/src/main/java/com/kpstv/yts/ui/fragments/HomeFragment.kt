package com.kpstv.yts.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentHomeBinding
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.activities.SearchActivity
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), TabLayout.OnTabSelectedListener {

    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = { requireActivity() }
    )
    private lateinit var binding: FragmentHomeBinding

    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity

        viewModel.homeView?.let {
            binding = FragmentHomeBinding.bind(it)
        } ?: run {
            binding = FragmentHomeBinding.bind(
                inflater.inflate(R.layout.fragment_home, container, false)
            )

            binding.searchImage.setOnClickListener {
                mainActivity.drawerLayout.openDrawer(GravityCompat.START)
            }

            binding.searchCard.setOnClickListener {
                val intent = Intent(mainActivity, SearchActivity::class.java)
                startActivity(intent)
            }

            binding.tabLayout.addOnTabSelectedListener(this)

            setFragment(ChartsFragment())

            viewModel.homeView = binding.root
        }

        return binding.root
    }

    override fun onTabReselected(tab: TabLayout.Tab?) { }

    override fun onTabUnselected(tab: TabLayout.Tab?) { }

    override fun onTabSelected(tab: TabLayout.Tab?) {
       if (tab?.position == 0) {
           setFragment(ChartsFragment())
       }else if (tab?.position == 1) {
           setFragment(GenreFragment())
       }
    }

    private fun setFragment(fragment: Fragment) {
        mainActivity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
