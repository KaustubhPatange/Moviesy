package com.kpstv.yts.extensions.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.AppInterface.Companion.TMDB_IMAGE_PREFIX
import com.kpstv.yts.AppInterface.Companion.handleRetrofitError
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.extensions.ExceptionCallback
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.MoviesCallback
import com.kpstv.yts.ui.activities.MoreActivity
import com.kpstv.yts.ui.fragments.sheets.BottomSheetQuickInfo
import com.kpstv.yts.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.custom_movie_layout.view.*

/** Usage:
 *  ------
 *  Create a new instance of this class
 *  Use injectViewAt function to add this on a layout
 *  Setup on of the follow backs as per the need
 *
 *  Features:
 *  ---------
 *  - Handles fetching of data and displaying to recyclerView
 *  with callbacks.
 *  - Automatically save & restore state (currently only for YTS movies)
 *    @see last section of this class
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
    private var isMoreAvailable: Boolean = false
    private var queryMap: Map<String, String>? = null
    private var mainViewModel: MainViewModel? = null
    private val viewModel: CustomViewModel? = if (context is AppCompatActivity)
        ViewModelProvider(context).get(CustomViewModel::class.java)
    else null

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
            intent.putExtra(MoreActivity.ARG_TITLE, titleText)
            intent.putExtra(MoreActivity.ARG_BASE_VALUE, base.toString())

            /** Passing empty endPoint for safe null checks */
            intent.putExtra(MoreActivity.ARG_ENDPOINT, "")

            val values = ArrayList<String>(queryMap.values)
            val keys = ArrayList<String>(queryMap.keys)

            intent.putExtra(MoreActivity.ARG_KEYS, keys)
            intent.putExtra(MoreActivity.ARG_VALUES, values)
            context.startActivity(intent)
        }
    }

    fun getTag() = titleText

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

    /** This is a special callback which will fetch featured movies
     *  from YTS website.
     */
    fun setupFeaturedCallbacks(
        viewModel: MainViewModel,
        onFailure: ExceptionCallback? = null
    ): Unit = with(context) {
        base = MovieBase.YTS
        mainViewModel = viewModel

        val listener = MoviesCallback(
            onFailure = { e ->
                e.printStackTrace()
                onFailure?.invoke(e)
            },
            onComplete = { movies, queryMap, isMoreAvailable ->
                this@CustomMovieLayout.isMoreAvailable = isMoreAvailable
                setupCallbacksNoMore(movies, queryMap, viewModel)
            }
        )

        if (!isRestoringConfiguration())
            viewModel.getFeaturedMovies(listener)
    }

    fun setupCallbacksNoMore(
        list: ArrayList<MovieShort>,
        queryMap: Map<String, String>,
        viewModel: MainViewModel? = null
    ) {
        base = MovieBase.YTS
        mainViewModel = viewModel
        this.queryMap = queryMap

        this.isMoreAvailable = false

        hideMoreCallbacks()

        models = list

        setupRecyclerView(models, viewModel)
    }

    @JvmName("setupCallbacks")
    fun setupCallbacks(viewModel: MainViewModel, queryMap: Map<String, String>) {
        base = MovieBase.YTS
        mainViewModel = viewModel
        this.queryMap = queryMap

        val listener = MoviesCallback(
            onFailure = { e ->
                handleRetrofitError(context, e)
                e.printStackTrace()
            },
            onComplete = { movies, map, isMoreAvailable ->
                this@CustomMovieLayout.isMoreAvailable = isMoreAvailable
                models = movies
                setupRecyclerView(models, viewModel)
                if (isMoreAvailable)
                    setupMoreButton(map)
                else hideMoreCallbacks()
            }
        )

        /** Restoring previous items from recyclerView */
        if (!isRestoringConfiguration()) {
            viewModel.getYTSQuery(listener, queryMap)
        }
    }

    /** Setting this will enable auto saving of state.
     *
     *  Make sure to pass [AppCompatActivity] as [context] while initializing this layout.
     * @param owner LifeCycleOwner of the view
     */
    fun setLifeCycleOwner(owner: LifecycleOwner?) {
        owner?.lifecycle?.addObserver(stateObserver)
    }

    /**
     * This will remove the data from the repository & also remove
     * any previous state configuration
     */
    fun removeData() {
        viewModel?.customModelMap?.remove(getTag())
        viewModel?.customLayoutRecyclerView?.remove(getTag())
        viewModel?.customLayoutConfig?.remove(getTag())
        val map = queryMap
        if (map != null)
            mainViewModel?.removeYtsQuery(map)
    }

    private fun setupMoreButton(queryMap: Map<String, String>) {

        /** Since our queryMap is a 2 dimension list, Intent class won't
         *  allow us to pass directly in extras.
         *
         *  Instead we will create two separate 1 dimension arraylist from it
         *  and pass them along with Intent.
         */

        val listener = View.OnClickListener {
            invokeMoreFunction(
                context,
                view.cm_text.text.toString(),
                queryMap
            )
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

    /** Usually for TMDB movies */
    private fun setupMoreButton(endPointUrl: String) {
        val listener = View.OnClickListener {
            val intent = Intent(context, MoreActivity::class.java)
            intent.putExtra(MoreActivity.ARG_TITLE, view.cm_text.text.toString())
            intent.putExtra(MoreActivity.ARG_ENDPOINT, endPointUrl)
            intent.putExtra(MoreActivity.ARG_BASE_VALUE, base.toString())
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
        Log.e(TAG, "${getTag()} -> ItemSize: ${adapter.itemCount}")

        if (list.isEmpty()) {
            view.visibility = View.GONE
        } else {
            restoreRecyclerViewState()
        }
    }

    /** Some methods for saving state of RecyclerView and maybe other stuff.
     *
     * What it does is it subscribe [stateObserver] to a lifecycle owner through
     * [setLifeCycleOwner] and save all the state to viewModel in onStop method.
     *
     * It then restore the state at appropriate places as well.
     */

    data class CustomLayoutConfig(
        val base: MovieBase, val queryMap: Map<String, String>?, val isMoreAvailable: Boolean
    )

    private fun isRestoringConfiguration(): Boolean {
        val config = viewModel?.customLayoutConfig?.get(getTag())
        val model = viewModel?.customModelMap?.get(getTag())
        return if (config != null && model != null && model.isNotEmpty()) {
            Log.e(TAG, "${getTag()} -> Restoring state & configurations")
            this.base = config.base
            this.queryMap = config.queryMap
            this.models = model
            this.isMoreAvailable = config.isMoreAvailable

            setupRecyclerView(models, mainViewModel)

            if (this.isMoreAvailable && config.queryMap != null)
                setupMoreButton(config.queryMap)
            else hideMoreCallbacks()
            true
        } else false
    }

    private fun restoreRecyclerViewState() {
        if (viewModel?.customLayoutRecyclerView?.containsKey(getTag()) == true)
            recyclerView.layoutManager?.onRestoreInstanceState(
                viewModel.customLayoutRecyclerView?.get(
                    getTag()
                )
            )
    }

    /** This observer will be bound to the lifecycle of the activity/fragment to
     *  automatically handle save state.
     *
     *  It will unsubscribe when the activity/fragment is destoryed.
     */
    private val stateObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)

            /** Saving state of recyclerView layout manager which includes scrollstate. */
            viewModel?.customLayoutRecyclerView?.put(
                getTag(),
                recyclerView.layoutManager?.onSaveInstanceState()
            )

            /** Saving recyclerView adapter items */
            if (recyclerView.adapter is CustomAdapter)
                viewModel?.customModelMap?.put(
                    getTag(),
                    (recyclerView.adapter as CustomAdapter).getModels()
                )

            /** Save custom layout configuration */
            viewModel?.customLayoutConfig?.put(
                getTag(),
                CustomLayoutConfig(
                    base = base,
                    queryMap = queryMap,
                    isMoreAvailable = isMoreAvailable
                )
            )
        }

        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            super.onDestroy(owner)
        }
    }
}