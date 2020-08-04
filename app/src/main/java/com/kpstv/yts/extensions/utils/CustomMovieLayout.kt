package com.kpstv.yts.extensions.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kpstv.yts.AppInterface.Companion.MOVIE_ID
import com.kpstv.yts.AppInterface.Companion.TMDB_IMAGE_PREFIX
import com.kpstv.yts.AppInterface.Companion.YTS_BASE_URL
import com.kpstv.yts.AppInterface.Companion.handleRetrofitError
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.utils.AppUtils.Companion.refactorYTSUrl
import com.kpstv.yts.interfaces.listener.MoviesListener
import com.kpstv.yts.ui.activities.FinalActivity
import com.kpstv.yts.ui.activities.MoreActivity
import com.kpstv.yts.ui.fragments.sheets.BottomSheetQuickInfo
import com.kpstv.yts.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.custom_movie_layout.view.*
import kotlinx.android.synthetic.main.item_common_banner.view.*
import kotlinx.android.synthetic.main.item_suggestion.view.*

/** Usage
 *
 *  Create a new instance of this class
 *  Use injectViewAt function to add this on a layout
 *  Setup on of the follow backs as per the need
 *
 *  @param context must be an activity to trigger item longClickListener
 */
class CustomMovieLayout(private val context: Context, private val titleText: String) {

    private val TAG = "CustomMovieLayout"
    private lateinit var view: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var base: MovieBase
    private lateinit var models: ArrayList<MovieShort>
    private lateinit var moreButton: ImageView
    private lateinit var clickableLayout: RelativeLayout
    private lateinit var removeBlock: () -> Unit

    companion object {
        /** Creating this companion object so that we can call it from
         *  other activities as well.
         */
        fun invokeMoreFunction(
            context: Context,
            titleText: String,
            queryMap: Map<String, String>,
            base: MovieBase = MovieBase.YTS
        ) {
            val intent = Intent(context, MoreActivity::class.java)
            intent.putExtra("title", titleText)
            intent.putExtra("baseValue", base.toString())

            /** Passing empty endPoint for safe null checks */
            intent.putExtra("endPoint", "")

            val values = ArrayList<String>(queryMap.values)
            val keys = ArrayList<String>(queryMap.keys)

            intent.putExtra("values", values)
            intent.putExtra("keys", keys)
            context.startActivity(intent)
        }
    }

    fun getView() = view

    fun removeView(parent: ViewGroup) {
        parent.removeView(view)
    }

    fun injectViewAt(parent: ViewGroup): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.custom_movie_layout, null)
            .also {
                view = it
                recyclerView = view.cm_recyclerView
                clickableLayout = view.mainLayout
                recyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                view.cm_text.text = titleText
                moreButton = view.cm_moreButton
                parent.addView(it)
            }
    }

    private fun hideMoreCallbacks() {
        moreButton.visibility = View.GONE
        clickableLayout.isClickable = false
        clickableLayout.isFocusable = false
    }

    fun setupCallbacksNoMore(
        list: ArrayList<MovieShort>,
        queryMap: Map<String, String>,
        viewModel: MainViewModel? = null
    ) {
        base = MovieBase.YTS
        hideMoreCallbacks()

        models = list

        setupRecyclerView(models, viewModel)

        removeBlock = { viewModel?.removeYtsQuery(queryMap) }
    }

    @JvmName("setupCallbacks")
    fun setupCallbacks(viewModel: MainViewModel, queryMap: Map<String, String>) {
        base = MovieBase.YTS

        val listener = object : MoviesListener {
            override fun onStarted() {
                // TODO: Show some progress bar
            }

            override fun onFailure(e: Exception) {
                handleRetrofitError(context, e)
                e.printStackTrace()
            }

            override fun onComplete(
                movies: ArrayList<MovieShort>,
                queryMap: Map<String, String>,
                isMoreAvailable: Boolean
            ) {
                models = movies
                setupRecyclerView(models, viewModel)
                if (isMoreAvailable)
                    setupMoreButton(queryMap)
                else hideMoreCallbacks()
            }

        }
        viewModel.getYTSQuery(listener, queryMap)
        removeBlock = { viewModel.removeYtsQuery(queryMap) }
    }

    /**
     * This will remove the data from the repository when force refreshed!
     */
    fun removeData() {
        if (::removeBlock.isInitialized)
            removeBlock.invoke()
    }

    private fun setupMoreButton(queryMap: Map<String, String>) {

        /** Since our queryMap is a 2 dimension arraylist, Intent class won't
         *  allow us to pass directly in extras.
         *
         *  Instead we will create two separate 1 dimension arraylist from it
         *  and pass them along with Intent.
         */

        val listener = View.OnClickListener {
            invokeMoreFunction(context, view.cm_text.text.toString(), queryMap)
        }

        moreButton.setOnClickListener(listener)
        clickableLayout.setOnClickListener(listener)
    }

    @JvmName("setupCallbacks1")
    fun setupCallbacks(list: ArrayList<TmDbMovie>, endPointUrl: String, isMore: Boolean) {
        base = MovieBase.TMDB
        models = ArrayList()
        list.forEach {
            if (it.release_date?.contains("-") == true) {
                models.add(
                    MovieShort(
                        movieId = it.id.toInt(),
                        title = it.title,
                        rating = it.rating,
                        year = it.release_date.split("-")[0].toInt(),
                        bannerUrl = "${TMDB_IMAGE_PREFIX}${it.bannerPath}",
                        runtime = it.runtime
                    )
                )
            }
        }
        setupRecyclerView(models)
        if (isMore) {
            setupMoreButton(endPointUrl)
        } else hideMoreCallbacks()
    }

    private fun setupMoreButton(endPointUrl: String) {

        val listener = View.OnClickListener {
            val intent = Intent(context, MoreActivity::class.java)
            intent.putExtra("title", view.cm_text.text.toString())
            intent.putExtra("endPoint", endPointUrl)
            intent.putExtra("baseValue", base.toString())
            context.startActivity(intent)
        }

        moreButton.setOnClickListener(listener)
        clickableLayout.setOnClickListener(listener)
    }

    private fun setupRecyclerView(list: ArrayList<MovieShort>, viewModel: MainViewModel? = null) {
        view.shimmerEffect.hideShimmer()
        view.shimmerEffect.visibility = View.GONE
        val adapter = CustomAdapter(context, list, base)

        if (viewModel != null && context is Activity)
            adapter.setOnLongListener = { movieShort, _ ->
                val sheet = BottomSheetQuickInfo()
                val bundle = Bundle()
                bundle.putSerializable("model", movieShort);
                sheet.arguments = bundle
                sheet.show((context as FragmentActivity).supportFragmentManager, "")
            }

        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        Log.e(TAG, "ItemSize: ${adapter.itemCount}")

        if (models.isEmpty()) {
            view.visibility = View.GONE
        }
    }

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

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onBindViewHolder(holder: CustomHolder, i: Int) {
            val movie = list[i]

            var imageUri = movie.bannerUrl
            if (!imageUri.contains(YTS_BASE_URL)) {
                imageUri = refactorYTSUrl(imageUri)
            }

            GlideApp.with(context.applicationContext).asBitmap().load(imageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        holder.mainImage.setImageBitmap(resource)
                        holder.itemView.shimmerFrame.hide()
                    }
                })

            holder.mainText.text = movie.title

            holder.mainCard.setOnClickListener {
                val intent = Intent(context, FinalActivity::class.java)
                when (base) {
                    MovieBase.YTS -> {
                        intent.putExtra(MOVIE_ID, movie.movieId)
                        context.startActivity(intent)
                    }
                    MovieBase.TMDB -> {

                        /** We are passing movie_id as string for TMDB Movie so that in
                         * Final View Model we can use the second route to get Movie Details*/

                        intent.putExtra(MOVIE_ID, "${movie.movieId}")
                        context.startActivity(intent)
                    }
                }
            }

            if (::setOnLongListener.isInitialized) {
                holder.mainCard.setOnLongClickListener {
                    setOnLongListener.invoke(movie, it)
                    return@setOnLongClickListener true
                }
            }
        }

        override fun getItemCount() = list.size

        class CustomHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mainCard = view.mainCard
            val mainText = view.mainText
            val mainImage = view.mainImage
        }
    }
}