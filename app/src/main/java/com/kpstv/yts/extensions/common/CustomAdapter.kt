package com.kpstv.yts.extensions.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.databinding.ItemSuggestionBinding
import com.kpstv.yts.extensions.load
import kotlinx.android.synthetic.main.item_common_banner.view.*
import kotlinx.android.synthetic.main.item_suggestion.view.*

class CustomAdapter(
    private val list: ArrayList<MovieShort>,
    private val onClick: (View, MovieShort) -> Unit
) :
    RecyclerView.Adapter<CustomAdapter.CustomHolder>() {

    lateinit var setOnLongListener: (MovieShort, View) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomHolder {
        return CustomHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(R.layout.item_suggestion, parent, false)
        )
    }

    fun getModels() = list

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CustomHolder, i: Int) {
        val movie = list[i]

        val imageUri = movie.bannerUrl

        holder.binding.shimmerImageView.isShimmering = true
        holder.binding.mainText.text = movie.title
        holder.binding.shimmerImageView.setOnClickListener { view ->
            onClick.invoke(view, movie)
        }
        holder.binding.root.doOnPreDraw {
            holder.binding.shimmerImageView.load(
                uri = imageUri,
                onSuccess = { bitmap ->
                    holder.binding.shimmerImageView.setImageBitmap(bitmap)
                    holder.binding.shimmerImageView.isShimmering = false
                }
            )
        }

        if (::setOnLongListener.isInitialized) {
            holder.binding.shimmerImageView.setOnLongClickListener {
                setOnLongListener.invoke(movie, it)
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount() = list.size

    class CustomHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSuggestionBinding.bind(view)
    }
}