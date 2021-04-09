package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.Navigator
import com.kpstv.yts.R
import com.kpstv.yts.adapters.WatchlistAdapter
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.databinding.FragmentWatchlistBinding
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WatchlistFragment2 : Fragment(R.layout.fragment_watchlist), Navigator.BottomNavigation.Callbacks {
    private val binding by viewBinding(FragmentWatchlistBinding::bind)
    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = ::requireParentFragment
    )
    private val navViewModel by activityViewModels<StartViewModel>()
    private lateinit var watchlistAdapter: WatchlistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBarLayout.applyTopInsets()
        setToolBar()
        bindUI()
        initRecyclerView()
    }

    override fun onReselected() {
        binding.recyclerView.smoothScrollToPosition(0)
    }

    private fun bindUI() {
        viewModel.favouriteMovieIds.observe(viewLifecycleOwner) { favourites: List<Model.response_favourite> ->
            watchlistAdapter.submitList(favourites)

            if (favourites.isNotEmpty()) {
                binding.layoutNoFavourite.root.hide()
            } else
                binding.layoutNoFavourite.root.show()

            // Restore previous state of recyclerView
            if (viewModel.uiState.watchFragmentState.recyclerViewState != null) {
                binding.recyclerView.layoutManager?.onRestoreInstanceState(viewModel.uiState.watchFragmentState.recyclerViewState)
                viewModel.uiState.watchFragmentState.recyclerViewState = null
            }
        }
    }

    private fun initRecyclerView() {
        watchlistAdapter = WatchlistAdapter(
            onClickListener = { model, _ ->
                navViewModel.goToDetail(ytsId = model.movieId)
            },
            onItemRemoveListener = { model, _ ->
                viewModel.removeFavourite(model.movieId)

                Snackbar.make(binding.root, getString(R.string.remove_watchlist), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.undo)) {
                        viewModel.addToFavourite(model)
                    }
                    .show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = watchlistAdapter
        }
    }

    private fun setToolBar() {
        val caller = parentFragment as MainFragmentDrawerCallbacks

        binding.toolbar.title = getString(R.string.watchlist)
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu)
        binding.toolbar.setNavigationOnClickListener {
            caller.openDrawer()
        }
        binding.toolbar.inflateMenu(R.menu.fragment_watchlist_menu)

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_search) {
                navViewModel.goToSearch()
            }
            return@setOnMenuItemClickListener true
        }

        /** Restoring AppBarLayout State */
        if (viewModel.uiState.watchFragmentState.isAppBarExpanded == false)
            binding.appBarLayout.collapse()
    }

    /**
     * Save all your view state here
     */
    override fun onStop() {
        super.onStop()
        viewModel.uiState.watchFragmentState.recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()
        viewModel.uiState.watchFragmentState.isAppBarExpanded = binding.appBarLayout.isAppBarExpanded
    }
}