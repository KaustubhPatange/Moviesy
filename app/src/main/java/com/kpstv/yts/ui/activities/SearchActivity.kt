package com.kpstv.yts.ui.activities

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.kpstv.yts.AppInterface.Companion.SUGGESTION_URL
import com.kpstv.yts.AppInterface.Companion.setAppThemeNoAction
import com.kpstv.yts.R
import com.kpstv.yts.adapters.CustomPagedAdapter
import com.kpstv.yts.adapters.SearchAdapter
import com.kpstv.yts.data.CustomDataSource.Companion.INITIAL_QUERY_FETCHED
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.extensions.utils.AppUtils.Companion.hideKeyboard
import com.kpstv.yts.extensions.utils.CustomMovieLayout
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.interfaces.listener.SingleClickListener
import com.kpstv.yts.interfaces.listener.SuggestionListener
import com.kpstv.yts.models.MovieShort
import com.kpstv.yts.models.TmDbMovie
import com.kpstv.yts.ui.activities.MoreActivity.Companion.base
import com.kpstv.yts.ui.activities.MoreActivity.Companion.queryMap
import com.kpstv.yts.ui.viewmodels.FinalViewModel
import com.kpstv.yts.ui.viewmodels.MoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search_single.*
import okhttp3.Request
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private val moreViewModel by viewModels<MoreViewModel>()
    private val finalViewModel by viewModels<FinalViewModel>()

    @Inject
    lateinit var retrofitUtils: RetrofitUtils


    private val TAG = "SearchActivity"

    private lateinit var suggestionFetch: Disposable
    private lateinit var suggestionAdapter: SearchAdapter

    private val suggestionModels = ArrayList<String>()
    private var isSearchClicked = false
    private lateinit var adapter: CustomPagedAdapter
    private var updateHandler = Handler()
    private var gridLayoutManager = GridLayoutManager(this, 3)
    private var linearLayoutManager = LinearLayoutManager(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppThemeNoAction(this)
        setContentView(R.layout.activity_search)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        title = " "

        /** Setting up FinalViewModel which will be used to show suggestions
         *  based on movie searched!
         */

        swipeRefreshLayout.isEnabled = false

        /** Hiding noMovieFound layout
         */
        layout_noMovieFound.hide()

        /** Setting suggestion RecyclerView and adapter with empty models
         */
        as_suggestionRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestionAdapter = SearchAdapter(this, suggestionModels)
        suggestionAdapter.setSingleClickListener(object : SingleClickListener {
            override fun onClick(obj: Any, i: Int) {
                updateQuery(obj as String)
            }

        })
        as_suggestionRecyclerView.adapter = suggestionAdapter

        /** Removing recyclerview first to see the content view first
         */
        removeSuggestionRecyclerView()

        /** Setting close button onClickListener (shown beside editText in appBar)
         */
        item_close.setOnClickListener {
            as_searchEditText.text.clear()
            it.hide()
        }

        as_searchEditText.setOnKeyListener(onActionSearchEvent())

        as_searchEditText.addTextChangedListener(searchEditTextChangeListener())

        setSuggestionObservable()
    }


    override fun onStart() {
        super.onStart()

        /** This focus will show the keyboard whenever this activity will be started
         */
        as_searchEditText.requestFocus()

        /** Hiding the close button at start
         */
        item_close.hide()
    }

    /** This will setup final RecylerView which will show all query.
     *
     *  Remember we've two types of handling searches.
     *
     *  1. When there is a single match: In this case single match is
     *     displayed with other info and suggested movies are shown.
     *
     *  2. When there are multiple match: In this case normal pagination
     *     will be used with fluctuation of showing suggested movies or not.
     */
    private fun setupRecyclerView() {
        swipeRefreshLayout.isRefreshing = true

        /** Used for clearing MoreViewModel current instance.
         */
        viewModelStore.clear()

        /** Remove any existing suggestion layout.
         */
        addLayout.removeAllViews()

        /** Setting up static queryMap and base of MoreActivity.
         */
        queryMap = YTSQuery.ListMoviesBuilder().apply {
            setQuery(as_searchEditText.text.toString())
        }.build()
        base = MovieBase.YTS

        Log.e(TAG, "==> Query: ${QueryConverter.fromMapToString(queryMap!!)}")

        adapter = CustomPagedAdapter(this, MovieBase.YTS)

        moreViewModel.itemPagedList?.observe(this, observer)

        /** Setting layout manager as grid layout at first we will update it
         *  to linear layout manager if there is only single match.
         */
        recyclerView.layoutManager = gridLayoutManager

        recyclerView.adapter = adapter

        updateHandler.postDelayed(updateTask, 1000)
    }

    /** This will be observing the live-data coming from DataSource.
     */
    private val observer = Observer<PagedList<MovieShort>?> {
        Log.e(TAG, "==> Called submit list")
        adapter.submitList(it)
    }

    /** A handler basically to hide Refreshing of swipeLayout but now
     *  serves more than the purpose.
     */
    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            try {
                if (recyclerView.adapter?.itemCount!! <= 0) {

                    /** A hack used to know if there are no results found
                     *  and certainly displaying noMovieFound layout.
                     */
                    if (INITIAL_QUERY_FETCHED) {
                        swipeRefreshLayout.isRefreshing = false
                        layout_noMovieFound.show()
                    } else
                        updateHandler.postDelayed(this, 1000)


                } else {

                    /** Query is successful and we've some results.
                     */

                    swipeRefreshLayout.isRefreshing = false

                    /** If there is only one item i.e single match, then linear layout manager
                     *  will be used and item_search_single_main will be displayed.
                     *
                     *  @see CustomPagedAdapter
                     */
                    if (adapter.itemCount == 1) {
                        recyclerView.layoutManager = linearLayoutManager
                    }

                    if (adapter.itemCount <= 10) {

                        /** This will show suggestion layout
                         */
                        adapter.currentList?.get(0)?.imdbCode?.let { imdbCode ->
                            finalViewModel.getSuggestions(object : SuggestionListener {
                                override fun onStarted() {} // Ignore AF...
                                override fun onComplete(
                                    movies: ArrayList<TmDbMovie>,
                                    tag: String?,
                                    isMoreAvailable: Boolean
                                ) {
                                    val layout = CustomMovieLayout(this@SearchActivity, "Suggested")
                                    layout.injectViewAt(addLayout)
                                    layout.setupCallbacks(
                                        movies,
                                        "${imdbCode}/similar",
                                        isMoreAvailable
                                    )
                                }

                                override fun onFailure(e: Exception) {} // Ignore exception
                            }, imdbCode)
                        }
                    }
                }
            } catch (e: Exception) {
                Toasty.error(applicationContext, "Error: ${e.message}").show()
            }
        }
    }

    /** This will update the recyclerView following certain events as well.
     */
    private fun updateQuery(text: String) {

        if (text.isEmpty()) {
            Toasty.error(this, "Query cannot be empty").show()
            return
        }

        /** Hiding noMovieFound layout if visible
         */
        layout_noMovieFound.hide()

        isSearchClicked = true
        /** Setting this boolean true because search was clicked */

        as_searchEditText.clearFocus()

        as_searchEditText.setText(text)
        /** Autocomplete text in search EditText */

        hideKeyboard(this)
        /** Hide the keyboard after search was clicked */

        removeSuggestionRecyclerView()

        setupRecyclerView()
    }

    /** This is a beauty of RxJava which allow use to watch textChange
     *  of editText on any thread we want.
     *
     *  This observable handles all text change of search EditText and
     *  updates the recyclerView using async Consumers.
     */
    private fun setSuggestionObservable() {
        suggestionFetch = RxTextView.textChanges(as_searchEditText)
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(onTaskStarted(), onTaskError())
    }

    /** Whenever user press search button on keyboard we will updateQuery
     */
    private fun onActionSearchEvent() = View.OnKeyListener { _, _, _ ->
        updateQuery(as_searchEditText.text.toString())
        true
    }

    /** This watcher is used to show and hide close button based on
     *  text query in search EditText
     */
    private fun searchEditTextChangeListener() = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s?.toString()?.isNotEmpty()!!)
                item_close.show()
            else item_close.hide()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }

    /** Instead of removing recyclerView we are clearing it's item models
     *  and notifying the adapter to display empty list.
     *
     *  We are also hiding the suggestionLayout (Relative layout) which owns
     *  suggestion RecyclerView.
     */
    private fun removeSuggestionRecyclerView() {
        suggestionModels.clear()
        suggestionAdapter.notifyDataSetChanged()
        suggestionLayout.hide()
    }

    /** Since Consumers are basically an async threads. Here we are making
     *  call to api using OkHttp for returning suggestion Json.
     *
     *  We will filter it and update the recyclerView adapter using
     *  runOnUiThread coroutine call.
     */
    private fun onTaskStarted() = Consumer<CharSequence> {

        /** Here, apart from checking if editText is empty we also have a boolean
         *  which serves an important purpose.
         *
         *  So suppose user typed some query in searchEdit text and suggestions are displayed
         *  in suggestion RecyclerView which follows this next path from => val response
         *
         *  Now whenever user click on suggestion it auto-completes the searchEdit text by
         *  changing the selected item text with current one. But RxJava is still watching
         *  this change and process it as a new query and runs the sequence ==> val response
         *  which eventually displays suggestion RecyclerView event after hiding it.
         *
         *  Hence in order to prevent it we are using a boolean which will detect if autocomplete
         *  was performed. If yes it won't run next code.
         */
        if (it.isEmpty() || isSearchClicked) {
            runOnUiThread {
                isSearchClicked = false
                removeSuggestionRecyclerView()
            }
            return@Consumer
        }

        val response = retrofitUtils.getHttpClient().newCall(
            Request.Builder()
                .url("${SUGGESTION_URL}$it")
                .build()
        ).execute()

        runOnUiThread {
            /** Showing the hidden suggestion Layout
             */
            suggestionLayout.show()

            suggestionModels.clear()
            try {
                val json = response.body?.string()
                if (json?.isNotEmpty()!!) {

                    val jsonArray = JSONArray(json).getJSONArray(1)
                    for (i in 0 until jsonArray.length()) {
                        suggestionModels.add(jsonArray.getString(i))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            suggestionAdapter.notifyDataSetChanged()
        }
    }

    /** This will catch an error (if any) thrown from RxBinding textView.
     */
    private fun onTaskError() = Consumer<Throwable> {
        it.printStackTrace()
    }

    /** For some reasons close Button was hiding once it lost its focus from
     *  search Edit Text, so in onResume we are showing it.
     */
    override fun onResume() {
        super.onResume()
        if (as_searchEditText.text.isNotEmpty()) {
            item_close.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (suggestionLayout.isVisible) {
            suggestionLayout.hide()
            hideKeyboard(this)
        } else
            onBackPressed()
        return true
    }


    override fun onDestroy() {
        super.onDestroy()

        /** Dispose the RxText watcher and remove handler callbacks
         *  for safer side.
         */
        if (::suggestionFetch.isInitialized)
            suggestionFetch.dispose()

        updateHandler.removeCallbacks(updateTask)
    }
}
