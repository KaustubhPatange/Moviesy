package com.kpstv.yts.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.Navigator
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentHomeBinding
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.ui.activities.SearchActivity
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment2 : Fragment(R.layout.fragment_home), TabLayout.OnTabSelectedListener, Navigator.BottomNavigation.Callbacks {

    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = ::requireParentFragment
    )

    interface Callbacks {
        fun doOnReselection() { }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val caller = parentFragment as MainFragmentDrawerCallbacks

        binding.apply {
            appBarLayout.applyTopInsets()
            searchImage.setOnClickListener {
                caller.openDrawer()
            }
            searchCard.setOnClickListener {
                val intent = Intent(requireContext(), SearchActivity::class.java) // TODO: Create a search fragment as well.
                startActivity(intent)
            }
            tvFilter.setOnClickListener {
                val queryMap = YTSQuery.ListMoviesBuilder.getDefault().apply {
                    setQuality(YTSQuery.Quality.q2160p)
                }.build()
                CustomMovieLayout.invokeMoreFunction(requireContext(), getString(R.string.search_filters), queryMap)
            }
            tabLayout.addOnTabSelectedListener(this@HomeFragment2)
        }

        // Restore UI state
        if (savedInstanceState == null) {
            setCurrentTab(viewModel.uiState.homeFragmentState.tabPosition)
        }
        if (viewModel.uiState.homeFragmentState.isAppBarExpanded == false)
            binding.appBarLayout.collapse()
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    override fun onTabSelected(tab: TabLayout.Tab?) {
        setCurrentTab(tab?.position)
    }

    override fun onReselected() {
        binding.appBarLayout.setExpanded(true)
        val fragment = childFragmentManager.findFragmentByTag(CURRENT_FRAGMENT) ?: return
        if (fragment is Callbacks) {
            fragment.doOnReselection()
        }
    }

    private fun setCurrentTab(position: Int?) {
        if (position == 0 || position == null) {
            binding.tabLayout.getTabAt(0)?.select()
            setFragment(ChartsFragment2())
        } else if (position == 1) {
            binding.tabLayout.getTabAt(1)?.select()
            setFragment(GenreFragment2())
        }
    }

    private fun setFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment, CURRENT_FRAGMENT)
            .commit()
    }

    override fun onStop() {
        super.onStop()
        // Save UI state
        viewModel.uiState.homeFragmentState.isAppBarExpanded =
            binding.appBarLayout.isAppBarExpanded
        viewModel.uiState.homeFragmentState.tabPosition =
            binding.tabLayout.selectedTabPosition
    }

    companion object {
        private const val CURRENT_FRAGMENT = "currentFragment"
    }
}