package com.kpstv.yts.extensions.common

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.databinding.ItemSuggestionBinding
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.load
import com.kpstv.yts.ui.activities.FinalActivity
import kotlinx.android.synthetic.main.item_common_banner.view.*
import kotlinx.android.synthetic.main.item_suggestion.view.*

class CustomAdapter(
    private val context: Context,
    private val list: ArrayList<MovieShort>,
    private val base: MovieBase
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
        holder.binding.shimmerImageView.load(
            uri = imageUri,
            onSuccess = { bitmap ->
                holder.binding.shimmerImageView.setImageBitmap(bitmap)
                holder.binding.shimmerImageView.isShimmering = false
            }
        )
        holder.binding.mainText.text = movie.title

        holder.binding.shimmerImageView.setOnClickListener { view ->
            scaleInAndOutAnimation(view)
            launchDetailScreen(movie)
        }

        if (::setOnLongListener.isInitialized) {
            holder.binding.shimmerImageView.setOnLongClickListener {
                setOnLongListener.invoke(movie, it)
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount() = list.size

    private fun scaleInAndOutAnimation(to: View) {
        to.animate().scaleX(1.2f).scaleY(1.2f)
            .withEndAction {
                to.scaleX = 1f
                to.scaleY = 1f
            }
            .start()
    }

    private fun launchDetailScreen(movie: MovieShort) {
        val intent = Intent(context, FinalActivity::class.java)
        when (base) {
            MovieBase.YTS -> {
                intent.putExtra(AppInterface.MOVIE_ID, movie.movieId)
                context.startActivity(intent)
            }
            MovieBase.TMDB -> {

                /** We are passing movie_id as string for TMDB Movie so that in
                 * Final View Model we can use the second route to get Movie Details*/

                intent.putExtra(AppInterface.MOVIE_ID, "${movie.movieId}")
                context.startActivity(intent)
            }
        }
    }

    class CustomHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSuggestionBinding.bind(view)
    }
}