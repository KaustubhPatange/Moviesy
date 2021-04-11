package com.kpstv.yts.extensions.common

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.navigation.Navigator
import com.kpstv.yts.AppInterface.Companion.handleRetrofitError
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.*
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.yts.ui.fragments.MoreFragment
import com.kpstv.yts.ui.fragments.sheets.BottomSheetQuickInfo
import com.kpstv.yts.ui.viewmodels.MainViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
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
 *  [fragmentManager] must be set using secondary constructor to trigger long click listeners
 *  on YTS movies only.
 */
class CustomMovieLayout(private val context: Context, private val titleText: String) {
    private var fragmentManager: FragmentManager? = null
    constructor(context: Context, fragmentManager: FragmentManager, titleText: String): this(context, titleText) {
        this.fragmentManager = fragmentManager
    }

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
    private var lifecycleOwner: LifecycleOwner? = null
    private var navViewModel: StartViewModel? = null

    private var recyclerViewClickListener: ((View, MovieShort) -> Unit)? = null

    fun getTag() = titleText

    fun removeView(parent: ViewGroup) {
        parent.removeView(view)
    }

    // Listen to click changes in the view
    fun listenForClicks(listener: (View, MovieShort) -> Unit) {
        this.recyclerViewClickListener = listener
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
        navViewModel: StartViewModel,
        viewModel: MainViewModel,
        onFailure: ExceptionCallback? = null
    ): Unit = with(context) {
        base = MovieBase.YTS
        mainViewModel = viewModel
        this@CustomMovieLayout.navViewModel = navViewModel

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

    @JvmName("setupCallbacks2")
    fun setupCallbacks(viewModel: MainViewModel, navViewModel: StartViewModel, queryMap: Map<String, String>) {
        base = MovieBase.YTS
        mainViewModel = viewModel
        this.queryMap = queryMap
        this.navViewModel = navViewModel

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
    fun setLifecycleOwner(owner: LifecycleOwner?) {
        this.lifecycleOwner = owner
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
            navViewModel?.goToMore(view.cm_text.text.toString(), queryMap)
        }

        moreButton.setOnClickListener(listener)
        clickableLayout.setOnClickListener(listener)
    }

    // For TmDb only
    @JvmName("setupCallbacks21")
    fun setupCallbacks(navViewModel: StartViewModel, list: ArrayList<TmDbMovie>, endPointUrl: String, isMore: Boolean) {
        this.navViewModel = navViewModel
        base = MovieBase.TMDB
        models = ArrayList()
        list.forEach {
            if (it.release_date?.contains("-") == true) {
                models.add(MovieShort.from(it))
            }
        }
        setupRecyclerView(models)
        if (isMore) {
            setupMoreButton(endPointUrl)
        } else hideMoreCallbacks()
    }

    // For TmDb only
    fun setupCallbacks(navViewModel: StartViewModel, title: String, list: List<Model.response_cast_movie.Cast>) {
        this.navViewModel = navViewModel
        base = MovieBase.TMDB
        models = ArrayList()
        list.forEach { cast ->
            models.add(MovieShort.from(cast))
        }
        models.removeAll { it.title == title }
        setupRecyclerView(models)

        hideMoreCallbacks()
    }

    /** Usually for TMDB movies */
    private fun setupMoreButton(endPointUrl: String) {
        val listener = View.OnClickListener {
            navViewModel?.navigateTo(
                screen = StartActivity.Screen.MORE,
                addToBackStack = true,
                transactionType = Navigator.TransactionType.ADD,
                args = MoreFragment.Args(
                    title = view.cm_text.text.toString(),
                    endPoint = endPointUrl,
                    movieBaseString = base.toString()
                ),
                transition = Navigator.TransitionType.FADE
            )
        }

        moreButton.setOnClickListener(listener)
        clickableLayout.setOnClickListener(listener)
    }

    // TODO: Set the bottom sheet that depends on activity.
    private fun setupRecyclerView(list: ArrayList<MovieShort>, viewModel: MainViewModel? = null) {
        view.shimmerEffect.hideShimmer()
        view.shimmerEffect.visibility = View.GONE
        val adapter = CustomAdapter(list) { view, movie ->
            val clicker = recyclerViewClickListener
            if (clicker != null) {
                clicker.invoke(view, movie)
            } else {
                when (base) {
                    MovieBase.YTS -> {
                        navViewModel?.goToDetail(ytsId = movie.movieId)
                    }
                    MovieBase.TMDB -> {
                        /** We are passing movie_id as string for TMDB Movie so that in
                         * Final View Model we can use the second route to get Movie Details*/
                        navViewModel?.goToDetail(tmDbId = movie.movieId.toString())
                    }
                }
            }
        }

        val fm = fragmentManager
        if (viewModel != null && fm != null)
            adapter.setOnLongListener = { movieShort, _ ->
                val sheet = BottomSheetQuickInfo()
                val bundle = Bundle()
                bundle.putSerializable("model", movieShort);
                sheet.arguments = bundle
                sheet.show(fm, "")
            }

        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        if (list.isEmpty()) {
            view.visibility = View.GONE
        } else {
            restoreRecyclerViewState()
        }
    }

    /** Some methods for saving state of RecyclerView and maybe other stuff.
     *
     * What it does is it subscribe [stateObserver] to a lifecycle owner through
     * [setLifecycleOwner] and save all the state to viewModel in onStop method.
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
                viewModel.customLayoutRecyclerView[getTag()]
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