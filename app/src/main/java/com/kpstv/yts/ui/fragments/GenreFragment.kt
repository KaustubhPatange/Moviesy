package com.kpstv.yts.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.drawableFrom
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentGenreBinding
import com.kpstv.yts.databinding.ItemLocalGenreBinding
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.add
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@Deprecated("Use v2")
@AndroidEntryPoint
class GenreFragment : Fragment(R.layout.fragment_genre), HomeFragment.HomeFragmentCallbacks {

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(FragmentGenreBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = LocalGenreAdapter(requireContext(), GENRE_CATEGORY_LIST) { model, _ ->
            val queryMap = YTSQuery.ListMoviesBuilder.getDefault().apply {
                setGenre(model.genre)
            }.build()

            /*CustomMovieLayout.invokeMoreFunction(
                requireContext(), "Based on ${model.title}", queryMap
            )*/
        }

        binding.recyclerView.adapter = adapter

        /** Restoring recyclerView state */
        binding.recyclerView.layoutManager?.onRestoreInstanceState(
            viewModel.uiState.genreFragmentState.recyclerViewState
        )
    }

    override fun doOnReselection() {
        binding.recyclerView.smoothScrollToPosition(0)
    }

    /**
     * Save state here
     */
    override fun onStop() {
        super.onStop()
        viewModel.uiState.genreFragmentState.recyclerViewState =
            binding.recyclerView.layoutManager?.onSaveInstanceState()
    }

    data class LocalGenreModel(
        val title: String, @DrawableRes val drawable: Int,
        val genre: YTSQuery.Genre
    )

    class LocalGenreAdapter(
        private val context: Context,
        private val list: List<LocalGenreModel>,
        private val listener: ((LocalGenreModel, Int) -> Unit)
    ) : RecyclerView.Adapter<LocalGenreAdapter.LocalGenreHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LocalGenreHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_local_genre,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: LocalGenreHolder, i: Int) {
            holder.binding.itemTitle.text = list[i].title
            holder.binding.itemImage.setImageDrawable(context.drawableFrom(list[i].drawable))
            holder.binding.root.setOnClickListener {

                listener(list[i], i)
            }
        }

        override fun getItemCount() = list.size

        class LocalGenreHolder(view: View) : RecyclerView.ViewHolder(view) {
            val binding = ItemLocalGenreBinding.bind(view)
        }
    }

    companion object {
        val GENRE_CATEGORY_LIST: List<LocalGenreModel> = ArrayList<LocalGenreModel>().apply {
            add("Action", R.drawable.ic_action_genre, YTSQuery.Genre.action)
            add("Adventure", R.drawable.ic_adventure_genre, YTSQuery.Genre.adventure)
            add("Animation", R.drawable.ic_animation_genre, YTSQuery.Genre.animation)
            add("Comedy", R.drawable.ic_comedy_genre, YTSQuery.Genre.comedy)
            add("Crime", R.drawable.ic_crime_genre, YTSQuery.Genre.crime)
            add("Documentary", R.drawable.ic_documentary_genre, YTSQuery.Genre.documentary)
            add("Drama", R.drawable.ic_drama_genre, YTSQuery.Genre.drama)
            add("Family", R.drawable.ic_family_genre, YTSQuery.Genre.family)
            add("Fantasy", R.drawable.ic_fantasy_genre, YTSQuery.Genre.fantasy)
            add("History", R.drawable.ic_history_genre, YTSQuery.Genre.history)
            add("Horror", R.drawable.ic_horror_genre, YTSQuery.Genre.horror)
            add("Musical", R.drawable.ic_musical_genre, YTSQuery.Genre.musical)
            add("Romance", R.drawable.ic_romance_genre, YTSQuery.Genre.romance)
            add("Sci-Fi", R.drawable.ic_sci_fi_genre, YTSQuery.Genre.sci_fi)
            add("Sports", R.drawable.ic_sport_genre, YTSQuery.Genre.sport)
            add("Thriller", R.drawable.ic_thriller_genre, YTSQuery.Genre.thriller)
            add("Western", R.drawable.ic_western_genre, YTSQuery.Genre.western)
        }.toList()
    }
}

