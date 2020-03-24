package com.kpstv.yts.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.danimahardhika.cafebar.CafeBar
import com.google.android.material.snackbar.Snackbar
import com.kpstv.yts.R
import com.kpstv.yts.activities.FinalActivity
import com.kpstv.yts.activities.MainActivity
import com.kpstv.yts.activities.SearchActivity
import com.kpstv.yts.adapters.WatchlistAdapter
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.models.response.Model
import kotlinx.android.synthetic.main.fragment_watchlist.view.*

class WatchlistFragment : Fragment() {

    private var v: View? = null
    private lateinit var mainActivity: MainActivity
    private val TAG = "WatchListFragment"
    private lateinit var adapter: WatchlistAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return v ?: inflater.inflate(R.layout.fragment_watchlist, container, false).also { view ->
            v = view
            mainActivity = activity as MainActivity

            setToolBar(view)

            initRecyclerView(view)

            bindUI(view)
        }
    }

    /** This will bind the fragment with the viewModel returning LiveData.
     */
    private fun bindUI(view: View) = Coroutines.main {
        mainActivity.viewModel.favouriteMovieIds.await().observe(mainActivity, Observer {
            adapter.updateModels(it)
            if (adapter.itemCount > 0) {
                view.layout_noFavourite.hide()
            } else
                view.layout_noFavourite.show()
        })
    }

    private fun initRecyclerView(view: View) {
        view.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WatchlistAdapter(mainActivity, ArrayList())
        adapter.onClickListener = { model, _ ->
            val intent = Intent(mainActivity, FinalActivity::class.java)
            intent.putExtra("movie_id", model.movieId)
            startActivity(intent)
        }

        adapter.onItemRemoveListener = { model, _ ->
            mainActivity.viewModel.removeFavourite(model.movieId)

            CafeBar.builder(context!!).apply {
                floating(true)
                content(getString(R.string.remove_watchlist))
                neutralText(getString(R.string.undo))
                onNeutral {
                    mainActivity.viewModel.addToFavourite(model)
                    it.dismiss()
                }
                autoDismiss(true)
                duration(CafeBar.Duration.LONG)
            }.show()
        }

        view.recyclerView.setHasFixedSize(true)
        view.recyclerView.adapter = adapter
    }

    private fun setToolBar(view: View) {
        view.toolbar.title = getString(R.string.watchlist)
        view.toolbar.setNavigationIcon(R.drawable.ic_menu)
        view.toolbar.setNavigationOnClickListener {
            mainActivity.drawerLayout.openDrawer(GravityCompat.START)
        }
        view.toolbar.inflateMenu(R.menu.fragment_watchlist_menu)

        view.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_search) {
                val intent = Intent(mainActivity, SearchActivity::class.java)
                startActivity(intent)
            }
            return@setOnMenuItemClickListener true
        }
    }

}
