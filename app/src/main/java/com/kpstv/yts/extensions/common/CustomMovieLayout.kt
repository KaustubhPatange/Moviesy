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
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.interfaces.listener.MoviesListener
import com.kpstv.yts.ui.activities.MoreActivity
import com.kpstv.yts.ui.fragments.sheets.BottomSheetQuickInfo
import com.kpstv.yts.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.custom_movie_layout.view.*

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
            override fun onStarted() {}

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

    /** Setting this will enable auto saving of state.
     *
     *  Make sure to pass [AppCompatActivity] as [context] while initializing this layout.
     * @param owner LifeCycleOwner of the view
     */
    fun setLifeCycleOwner(owner: LifecycleOwner?) {
        owner?.lifecycle?.addObserver(stateObserver)
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
        val adapter =
            CustomAdapter(
                context,
                list,
                base
            )

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

    private fun restoreRecyclerViewState() {
        if (viewModel?.customLayoutMap?.containsKey(getTag()) == true)
            recyclerView.layoutManager?.onRestoreInstanceState(viewModel.customLayoutMap?.get(getTag()))
    }

    private val stateObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            if (viewModel?.customLayoutMap == null)
                viewModel?.customLayoutMap = HashMap()
            viewModel?.customLayoutMap?.put(
                getTag(),
                recyclerView.layoutManager?.onSaveInstanceState()
            )
        }

        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            super.onDestroy(owner)
        }
    }
}