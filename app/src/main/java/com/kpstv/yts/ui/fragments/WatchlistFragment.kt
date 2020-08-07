package com.kpstv.yts.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.danimahardhika.cafebar.CafeBar
import com.kpstv.yts.AppInterface.Companion.MOVIE_ID
import com.kpstv.yts.R
import com.kpstv.yts.ui.activities.FinalActivity
import com.kpstv.yts.ui.activities.MainActivity
import com.kpstv.yts.ui.activities.SearchActivity
import com.kpstv.yts.adapters.WatchlistAdapter
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.databinding.FragmentWatchlistBinding
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_watchlist.view.*

@AndroidEntryPoint
class WatchlistFragment : Fragment() {

    private val viewModel by viewModels<MainViewModel>(
        ownerProducer = { requireActivity() }
    )
    private lateinit var binding: FragmentWatchlistBinding

    private lateinit var mainActivity: MainActivity

    private val TAG = "WatchListFragment"
    private lateinit var adapter: WatchlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity

         viewModel.watchView?.let {
          binding = FragmentWatchlistBinding.bind(it)
        } ?: run {
             binding = FragmentWatchlistBinding.bind(
                 inflater.inflate(R.layout.fragment_watchlist, container, false)
             )

             binding.layoutNoFavourite.hide()

             setToolBar()

             initRecyclerView()

             bindUI()

             viewModel.watchView = binding.root
         }
        return binding.root
    }

    /** This will bind the fragment with the viewModel returning LiveData.
     */
    private fun bindUI() = Coroutines.main {
        viewModel.favouriteMovieIds.await().observe(viewLifecycleOwner, Observer {
            adapter.updateModels(it)
            if (adapter.itemCount > 0) {
                binding.layoutNoFavourite.hide()
            } else
                binding.layoutNoFavourite.show()
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WatchlistAdapter(requireContext(), ArrayList())
        adapter.onClickListener = { model, _ ->
            val intent = Intent(requireContext(), FinalActivity::class.java)
            intent.putExtra(MOVIE_ID, model.movieId)
            startActivity(intent)
        }

        adapter.onItemRemoveListener = { model, _ ->
            viewModel.removeFavourite(model.movieId)

            CafeBar.builder(mainActivity).apply {
                floating(true)
                content(getString(R.string.remove_watchlist))
                neutralText(getString(R.string.undo))
                onNeutral {
                    viewModel.addToFavourite(model)
                    it.dismiss()
                }
                autoDismiss(true)
                duration(CafeBar.Duration.SHORT)
            }.show()
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter
    }

    private fun setToolBar() {
        binding.toolbar.title = getString(R.string.watchlist)
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu)
        binding.toolbar.setNavigationOnClickListener {
            mainActivity.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.toolbar.inflateMenu(R.menu.fragment_watchlist_menu)

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_search) {
                val intent = Intent(mainActivity, SearchActivity::class.java)
                startActivity(intent)
            }
            return@setOnMenuItemClickListener true
        }
    }
}
