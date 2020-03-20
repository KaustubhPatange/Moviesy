package com.kpstv.yts.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.kpstv.yts.R
import com.kpstv.yts.YTSQuery
import com.kpstv.yts.activities.MainActivity
import com.kpstv.yts.interfaces.listener.MoviesListener
import com.kpstv.yts.models.MovieShort
import com.kpstv.yts.utils.CustomMovieLayout

class ChartsFragment : Fragment() {

    private var v: View? = null
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return v ?: inflater.inflate(R.layout.fragment_charts, container, false).also { view ->

            mainActivity = activity as MainActivity

            v = view

            setViewAndLayout()
        }
    }

    private fun setViewAndLayout() {
        val addLayout = v?.findViewById<LinearLayout>(R.id.addLayout)!!

        val featureLayout = CustomMovieLayout(mainActivity, "Featured")
        featureLayout.injectViewAt(addLayout)

        mainActivity.viewModel.getFeaturedMovies(object : MoviesListener {
            override fun onStarted() {

            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
                featureLayout.removeView(addLayout)
            }

            override fun onComplete(
                movies: ArrayList<MovieShort>,
                queryMap: Map<String, String>,
                isMoreAvailable: Boolean
            ) {
                featureLayout.setupCallbacksNoMore(movies)
            }

        })

        val queryMap = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.rating)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        val layout = CustomMovieLayout(mainActivity, "Top Rated")
        layout.injectViewAt(addLayout)
        layout.setupCallbacks( mainActivity.viewModel, queryMap)

        val queryMap2 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.seeds)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        val layout1 = CustomMovieLayout(mainActivity, "Top Today")
        layout1.injectViewAt(addLayout)
        layout1.setupCallbacks( mainActivity.viewModel, queryMap2)

        val queryMap3 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.download_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        val layout2 = CustomMovieLayout(mainActivity, "Popular")
        layout2.injectViewAt(addLayout)
        layout2.setupCallbacks( mainActivity.viewModel, queryMap3)

        val queryMap4 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.like_count)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        val layout3 = CustomMovieLayout(mainActivity, "Most liked")
        layout3.injectViewAt(addLayout)
        layout3.setupCallbacks( mainActivity.viewModel, queryMap4)

        val queryMap5 = YTSQuery.ListMoviesBuilder().apply {
            setSortBy(YTSQuery.SortBy.year)
            setOrderBy(YTSQuery.OrderBy.descending)
        }.build()

        val layout4 = CustomMovieLayout(mainActivity, "Latest")
        layout4.injectViewAt(addLayout)
        layout4.setupCallbacks( mainActivity.viewModel, queryMap5)
    }

}
