package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.databinding.FragmentChartsBinding
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.utils.CustomMovieLayout
import com.kpstv.yts.interfaces.listener.MoviesListener
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class ChartsFragment : Fragment() {

    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = { requireActivity() }
    )

    private lateinit var binding: FragmentChartsBinding

    private lateinit var cmlFeatured: CustomMovieLayout

    private lateinit var cmlTopRated: CustomMovieLayout
    private lateinit var cmlTopToday: CustomMovieLayout
    private lateinit var cmlPopular: CustomMovieLayout
    private lateinit var cmlMostLiked: CustomMovieLayout
    private lateinit var cmlLatest: CustomMovieLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (::binding.isInitialized) return binding.root
        else {
            binding = FragmentChartsBinding.bind(
                inflater.inflate(R.layout.fragment_charts, container, false)
            )

            setViewAndLayout()
            setSwipeRefreshCallback()
        }
        return binding.root
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
        }

        viewModel.getFeaturedMovies(object : MoviesListener {
            override fun onStarted() {}

            override fun onFailure(e: Exception) {
                e.printStackTrace()
                cmlFeatured.removeView(binding.addLayout)
                Toasty.warning(requireActivity(), getString(R.string.featured_movies)).show()
            }

            override fun onComplete(
                movies: ArrayList<MovieShort>,
                queryMap: Map<String, String>,
                isMoreAvailable: Boolean
            ) {
                cmlFeatured.setupCallbacksNoMore(movies, queryMap, viewModel)
            }
        })

        /** Top Rated Layout */
        val queryMap = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.rating)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlTopRated = CustomMovieLayout(requireActivity(), getString(R.string.top_rated)).apply {
            injectViewAt(binding.addLayout)
            setupCallbacks(viewModel, queryMap)
        }

        /** Top Today Layout */
        val queryMap2 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.seeds)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlTopToday = CustomMovieLayout(requireActivity(), getString(R.string.top_today)).apply {
            injectViewAt(binding.addLayout)
            setupCallbacks(viewModel, queryMap2)
        }

        /** Popular Layout */
        val queryMap3 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.download_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlPopular = CustomMovieLayout(requireActivity(), getString(R.string.popular)).apply {
            injectViewAt(binding.addLayout)
            setupCallbacks(viewModel, queryMap3)
        }

        /** Most Liked Layout */
        val queryMap4 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.like_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlMostLiked = CustomMovieLayout(requireActivity(), getString(R.string.most_liked)).apply {
            injectViewAt(binding.addLayout)
            setupCallbacks(viewModel, queryMap4)
        }

        /** Latest Layout */
        val queryMap5 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.year)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        cmlLatest = CustomMovieLayout(requireActivity(), getString(R.string.latest)).apply {
            injectViewAt(binding.addLayout)
            setupCallbacks(viewModel, queryMap5)
        }
    }
}
