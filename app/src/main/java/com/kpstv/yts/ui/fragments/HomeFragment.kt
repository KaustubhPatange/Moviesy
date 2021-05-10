package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.*
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentHomeBinding
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.reflect.KClass

@AndroidEntryPoint
class HomeFragment : ValueFragment(R.layout.fragment_home), Navigator.Navigation.Callbacks, NavigatorTransmitter {
    private lateinit var navigator: Navigator
    private lateinit var tabController: TabNavigationController

    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = ::requireParentFragment
    )
    private val navViewModel by activityViewModels<StartViewModel>()

    interface Callbacks {
        fun doOnReselection() { }
    }

    override fun getNavigator(): Navigator = navigator

    override val forceBackPress: Boolean
        get() = binding.tabLayout.selectedTabPosition != 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val caller = parentFragment as MainFragmentDrawerCallbacks
        navigator = Navigator.with(this, savedInstanceState).initialize(binding.fragmentContainer)
        tabController = navigator.install(object: Navigator.TabNavigation() {
            override val tabLayoutId: Int = R.id.tabLayout
            override val tabNavigationFragments: List<KClass<out Fragment>> = listOf(
                ChartsFragment::class,
                GenreFragment::class
            )
            override val fragmentNavigationTransition: Animation = Animation.Slide
            override val fragmentViewRetentionType: ViewRetention = ViewRetention.RETAIN
        })

        binding.apply {
            appBarLayout.applyTopInsets()
            searchImage.setOnClickListener {
                caller.openDrawer()
            }
            searchCard.setOnClickListener {
                navViewModel.goToSearch(SearchFragment.createSharedPayload(it, binding.textView))
            }
            tvFilter.setOnClickListener {
                val queryMap = YTSQuery.ListMoviesBuilder.getDefault().apply {
                    setQuality(YTSQuery.Quality.q2160p)
                }.build()
                navViewModel.goToMore(getString(R.string.search_filters), queryMap)
            }
        }

        if (viewModel.uiState.homeFragmentState.isAppBarExpanded == false)
            binding.appBarLayout.collapse()
    }

    override fun onReselected() {
        binding.appBarLayout.setExpanded(true)
        val fragment = navigator.getCurrentFragment()
        if (fragment is Callbacks) {
            fragment.doOnReselection()
        }
    }

    override fun onStop() {
        super.onStop()
        // Save UI state
        viewModel.uiState.homeFragmentState.isAppBarExpanded = binding.appBarLayout.isAppBarExpanded
        viewModel.uiState.homeFragmentState.tabPosition = binding.tabLayout.selectedTabPosition
    }

    override fun onBackPressed(): Boolean {
        if (binding.tabLayout.selectedTabPosition != 0) {
            tabController.select(0)
            return true
        }
        return super.onBackPressed()
    }
}