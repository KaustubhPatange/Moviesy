package com.kpstv.yts.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.imageloaderview.ImageLoaderView
import com.kpstv.yts.AppInterface.Companion.IS_DARK_THEME
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getBulletSymbol
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getImdbUrl
import com.kpstv.yts.extensions.utils.AppUtils.Companion.launchUrl
import com.kpstv.yts.extensions.load
import com.kpstv.yts.ui.viewmodels.StartViewModel

/** An adapter class to manage the pagination library
 */
class CustomPagedAdapter(
    private val navViewModel: StartViewModel,
    private val base: MovieBase
) :
    PagedListAdapter<MovieShort, CustomPagedAdapter.CustomHolder>(DIFF_CALLBACK) {

    private val TAG = javaClass.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        /** We will show different layout when the item count is only one.
         *  This is implemented in SearchActivity
         */
        if (itemCount == 1) {
            CustomHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_search_single_main, parent, false
                )
            )
        } else {
            CustomHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_more, parent, false
                )
            )
        }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CustomHolder, position: Int) {
        val movie = getItem(position)

        if (movie != null) {

            /** This are some extra stuffs we are doing when count it one
             *  i.e there is only single search
             */
            if (itemCount == 1) {
                holder.mainSubTextView.text =
                    "${movie.year} ${getBulletSymbol()} ${movie.runtime} mins"
                holder.mainImdbButton.text = "imdb ${movie.rating}"
                holder.mainImdbButton.setOnClickListener { view ->
                    movie.imdbCode?.let { launchUrl(view.context, getImdbUrl(it), IS_DARK_THEME) }
                }
                holder.mainLayout.setOnClickListener {
                    navViewModel.goToDetail(
                        ytsId = movie.movieId, add = true
                    )
                }
            }

            holder.mainImage.load(
                uri = movie.bannerUrl,
                onSuccess = { resource ->
                    holder.mainImage.setImageBitmap(resource)
                    holder.mainImage.isShimmering = false
                },
                onError = {
                    Log.e(TAG, "=> Glide failed for: ${movie.title}")
                }
            )

            holder.mainText.text = movie.title

            holder.mainImage.setOnClickListener {
                when (base) {
                    MovieBase.YTS -> {
                        navViewModel.goToDetail(ytsId = movie.movieId, add = true)
                    }
                    MovieBase.TMDB -> {
                        /** We are passing movie_id as string for TMDB Movie so that in
                         * Final View Model we can use the second route to get Movie Details*/
                        navViewModel.goToDetail(tmDbId = movie.movieId.toString(), add = true)
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<MovieShort?> =
            object : DiffUtil.ItemCallback<MovieShort?>() {
                override fun areItemsTheSame(oldItem: MovieShort, newItem: MovieShort) =
                    oldItem.imdbCode == newItem.imdbCode

                override fun areContentsTheSame(oldItem: MovieShort, newItem: MovieShort) =
                    oldItem == newItem
            }
    }

    /** Some of the Id does not exist in either one of view. That's why we are
     *  using standard findViewById so that even though view doesn't exist it
     *  will be null instead of producing crash which synthetic binding would cause.
     */
    class CustomHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mainText = view.findViewById<TextView>(R.id.mainText)
        val mainImage = view.findViewById<ImageLoaderView>(R.id.mainImage)

        val mainLayout = view.findViewById<ConstraintLayout>(R.id.mainLayout)
        val mainSubTextView = view.findViewById<TextView>(R.id.mainSubText)
        val mainImdbButton = view.findViewById<Button>(R.id.mainImdbButton)
    }
}