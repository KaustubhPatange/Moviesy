package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentChartsBinding
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@Deprecated("Use v2")
@AndroidEntryPoint
class ChartsFragment : Fragment(R.layout.fragment_charts), HomeFragment.HomeFragmentCallbacks {

    private val viewModel by viewModels<MainViewModel>()

    private val binding by viewBinding(FragmentChartsBinding::bind)
    private lateinit var cmlFeatured: CustomMovieLayout

    private lateinit var cmlRecent: CustomMovieLayout
    private lateinit var cmlTopRated: CustomMovieLayout
    private lateinit var cmlPopular: CustomMovieLayout
    private lateinit var cmlMostLiked: CustomMovieLayout
    private lateinit var cmlLatest: CustomMovieLayout
    private val TAG = javaClass.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewAndLayout()
        setSwipeRefreshCallback()

        /** Restoring previous state of nestedScrollView */
        binding.nestedScrollView.onRestoreInstanceState(
            viewModel.uiState_old.chartFragmentState.nestedScrollState
        )
    }

    override fun doOnReselection() {
        binding.nestedScrollView.smoothScrollTo(0,0)
    }

    private fun setSwipeRefreshCallback() {
        /** When pull down to refresh is called, all data in the layout
         *  will be re-fetched.
         */
        binding.root.setOnRefreshListener {
            resetAllViewData()
            setViewAndLayout()

            binding.root.isRefreshing = false
        }
    }

    private fun resetAllViewData() {
        cmlFeatured.removeData()
        cmlRecent.removeData()
        cmlTopRated.removeData()
        cmlPopular.removeData()
        cmlMostLiked.removeData()
        cmlLatest.removeData()

        binding.addLayout.removeAllViews()
    }

    private fun setViewAndLayout() {
        // FIXME: (Needs Investigation) Sometimes getActivity() returns null & thus it crashes
        // val context = if (activity != null) requireActivity() else if (context != null) requireContext() else return

        /** Featured Layout */
       /* cmlFeatured = CustomMovieLayout(requireActivity(), getString(R.string.featured)).apply {
            injectViewAt(binding.addLayout)
            setLifecycleOwner(viewLifecycleOwner)
            setupFeaturedCallbacks(viewModel) {
                cmlFeatured.removeView(binding.addLayout)
                Toasty.warning(requireContext(), getString(R.string.featured_movies)).show()
            }
        }

        *//** Recently Added Layout *//*
        val queryMap6 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.date_added)
        }.build()

        cmlRecent = CustomMovieLayout(requireActivity(), getString(R.string.recently_added)).apply {
            injectViewAt(binding.addLayout)
            setLifecycleOwner(viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap6)
        }

        *//** Top Rated Layout *//*
        val queryMap = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.rating)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlTopRated = CustomMovieLayout(requireActivity(), getString(R.string.top_rated)).apply {
            injectViewAt(binding.addLayout)
            setLifecycleOwner(viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap)
        }

        *//** Popular Layout *//*
        val queryMap3 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.download_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlPopular = CustomMovieLayout(requireActivity(), getString(R.string.popular)).apply {
            injectViewAt(binding.addLayout)
            setLifecycleOwner(viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap3)
        }

        *//** Most Liked Layout *//*
        val queryMap4 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.like_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlMostLiked = CustomMovieLayout(requireActivity(), getString(R.string.most_liked)).apply {
            injectViewAt(binding.addLayout)
            setLifecycleOwner(viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap4)
        }

        *//** Latest Layout *//*
        val queryMap5 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.year)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlLatest = CustomMovieLayout(requireActivity(), getString(R.string.latest)).apply {
            injectViewAt(binding.addLayout)
            setLifecycleOwner(viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap5)
        }*/
    }

    /**
     * Save your state here.
     */
    override fun onStop() {
        super.onStop()
        viewModel.uiState_old.chartFragmentState.nestedScrollState =
            binding.nestedScrollView.onSaveInstanceState()
    }
}
