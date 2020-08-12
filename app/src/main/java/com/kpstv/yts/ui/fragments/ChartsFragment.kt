package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.databinding.FragmentChartsBinding
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.interfaces.listener.MoviesListener
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class ChartsFragment : Fragment(R.layout.fragment_charts) {

    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = { requireActivity() }
    )

    private val binding by viewBinding(FragmentChartsBinding::bind)
    private lateinit var cmlFeatured: CustomMovieLayout

    private lateinit var cmlTopRated: CustomMovieLayout
    private lateinit var cmlTopToday: CustomMovieLayout
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
            viewModel.chartFragmentState.nestedScrollState
        )
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
        cmlTopRated.removeData()
        cmlTopToday.removeData()
        cmlPopular.removeData()
        cmlMostLiked.removeData()
        cmlLatest.removeData()

        binding.addLayout.removeAllViews()
    }

    private fun setViewAndLayout() {
        /** Featured Layout */
        cmlFeatured = CustomMovieLayout(requireActivity(), getString(R.string.featured)).apply {
            injectViewAt(binding.addLayout)
            setLifeCycleOwner(requireHomeFragment()?.viewLifecycleOwner)
            setupFeaturedCallbacks(viewModel) {
                cmlFeatured.removeView(binding.addLayout)
                Toasty.warning(requireActivity(), getString(R.string.featured_movies)).show()
            }
        }

        /** Top Rated Layout */
        val queryMap = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.rating)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlTopRated = CustomMovieLayout(requireActivity(), getString(R.string.top_rated)).apply {
            injectViewAt(binding.addLayout)
            setLifeCycleOwner(requireHomeFragment()?.viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap)
        }

        /** Top Today Layout */
        val queryMap2 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.seeds)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlTopToday = CustomMovieLayout(requireActivity(), getString(R.string.top_today)).apply {
            injectViewAt(binding.addLayout)
            setLifeCycleOwner(requireHomeFragment()?.viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap2)
        }

        /** Popular Layout */
        val queryMap3 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.download_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlPopular = CustomMovieLayout(requireActivity(), getString(R.string.popular)).apply {
            injectViewAt(binding.addLayout)
            setLifeCycleOwner(requireHomeFragment()?.viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap3)
        }

        /** Most Liked Layout */
        val queryMap4 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.like_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlMostLiked = CustomMovieLayout(requireActivity(), getString(R.string.most_liked)).apply {
            injectViewAt(binding.addLayout)
            setLifeCycleOwner(requireHomeFragment()?.viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap4)
        }

        /** Latest Layout */
        val queryMap5 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.year)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlLatest = CustomMovieLayout(requireActivity(), getString(R.string.latest)).apply {
            injectViewAt(binding.addLayout)
            setLifeCycleOwner(requireHomeFragment()?.viewLifecycleOwner)
            setupCallbacks(viewModel, queryMap5)
        }
    }

    /**
     * Seems like parent fragment of this FragmentContainerView is NavHostFragment.
     * We need to find the HomeFragment from it's child fragment.
     *
     * This will be used to pass LifeCycleOwner to CustomMovieLayout (above) to
     * automatically save the state of the fragment.
     *
     * I can't pass this fragment's lifeCycle since it's anonymous behaviour is not
     * properly invoking onStop() and onDestroy() as I want.
     */
    private fun requireHomeFragment() =
        (requireParentFragment() as NavHostFragment)
            .childFragmentManager.fragments.firstOrNull {
                it.javaClass.name == HomeFragment::class.java.name
            }

    /**
     * Save your state here.
     */
    override fun onStop() {
        super.onStop()
        viewModel.chartFragmentState.nestedScrollState =
            binding.nestedScrollView.onSaveInstanceState()
    }
}
