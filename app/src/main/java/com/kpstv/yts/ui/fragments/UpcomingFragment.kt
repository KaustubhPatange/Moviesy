package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.ValueFragment
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentUpcomingBinding
import com.kpstv.yts.ui.controllers.upcoming.UpcomingController
import com.kpstv.yts.ui.viewmodels.StartViewModel
import com.kpstv.yts.ui.viewmodels.UpcomingUiState
import com.kpstv.yts.ui.viewmodels.UpcomingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpcomingFragment : ValueFragment(R.layout.fragment_upcoming), HomeFragment.Callbacks {

    private val binding by viewBinding(FragmentUpcomingBinding::bind)
    private val viewModel by viewModels<UpcomingViewModel>()
    private val navViewModel by activityViewModels<StartViewModel>()

    private val controller: UpcomingController by lazy {
        UpcomingController(requireContext(), goToDetail = { movieUrl ->
            navViewModel.goToDetail(movieUrl = movieUrl)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchMovies(forceRefresh = true)
        }
        binding.rvEpoxy.setControllerAndBuildModels(controller)

        fetchMovies()
    }

    private fun fetchMovies(forceRefresh: Boolean = false) {
        viewModel.fetchUpcomingMovies(forceRefresh).observe(viewLifecycleOwner) { state ->
            when(state) {
                UpcomingUiState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                is UpcomingUiState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    controller.submitUpcomingModels(state.data)
                }
                UpcomingUiState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    override fun doOnReselection() {
        val dy = binding.rvEpoxy.computeVerticalScrollOffset()
        binding.rvEpoxy.smoothScrollBy(0, -dy)
    }
}