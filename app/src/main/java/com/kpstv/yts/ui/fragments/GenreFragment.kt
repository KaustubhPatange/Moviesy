package com.kpstv.yts.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.AppInterface.Companion.GENRE_CATEGORY_LIST
import com.kpstv.yts.R
import com.kpstv.yts.YTSQuery
import com.kpstv.yts.extensions.utils.CustomMovieLayout
import kotlinx.android.synthetic.main.fragment_genre.view.*
import kotlinx.android.synthetic.main.item_local_genre.view.*

class GenreFragment : Fragment() {

    private var v: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return v ?: inflater.inflate(R.layout.fragment_genre, container, false).also { view ->
            view.recyclerView.layoutManager = LinearLayoutManager(view.context)

            val adapter = LocalGenreAdapter(view.context, GENRE_CATEGORY_LIST) { model, _ ->
                val queryMap = YTSQuery.ListMoviesBuilder().apply {
                    setGenre(model.genre)
                    setOrderBy(YTSQuery.OrderBy.descending)
                }.build()

                CustomMovieLayout.invokeMoreFunction(
                    view.context, "Based on ${model.title}", queryMap
                )
            }

            view.recyclerView.adapter = adapter

            v = view
        }
    }


    data class LocalGenreModel(
        val title: String, @DrawableRes val drawable: Int,
        val genre: YTSQuery.Genre
    )

    class LocalGenreAdapter(
        private val context: Context,
        private val list: ArrayList<LocalGenreModel>,
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
            holder.title.text = list[i].title
            holder.image.setImageDrawable(
                context.getDrawable(list[i].drawable)
            )
            holder.mainLayout.setOnClickListener {

                listener(list[i], i)
            }
        }

        override fun getItemCount() = list.size

        class LocalGenreHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mainLayout = view.mainLayout
            val title = view.item_title
            val image = view.item_image
        }

    }
}

