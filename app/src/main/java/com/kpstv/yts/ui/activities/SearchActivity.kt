package com.kpstv.yts.ui.activities

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.setAppThemeNoAction
import com.kpstv.yts.R
import com.kpstv.yts.adapters.CustomPagedAdapter
import com.kpstv.yts.adapters.HistoryModel
import com.kpstv.yts.adapters.SearchAdapter
import com.kpstv.yts.data.CustomDataSource.Companion.INITIAL_QUERY_FETCHED
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.databinding.ActivitySearchBinding
import com.kpstv.yts.extensions.*
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.extensions.utils.AppUtils.Companion.hideKeyboard
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.ui.activities.MoreActivity.Companion.base
import com.kpstv.yts.ui.activities.MoreActivity.Companion.queryMap
import com.kpstv.yts.ui.helpers.AdaptiveSearchHelper
import com.kpstv.yts.ui.helpers.SuggestionHelper
import com.kpstv.yts.ui.viewmodels.FinalViewModel
import com.kpstv.yts.ui.viewmodels.MoreViewModel
import com.kpstv.yts.ui.viewmodels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private val moreViewModel by viewModels<MoreViewModel>()
    private val finalViewModel by viewModels<FinalViewModel>()
    private val searchViewModel by viewModels<SearchViewModel>()

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    @Inject
    lateinit var retrofitUtils: RetrofitUtils

    @Inject
    lateinit var suggestionHelper: SuggestionHelper

    private val TAG = "SearchActivity"

    private lateinit var suggestionFetch: Disposable
    private lateinit var suggestionAdapter: SearchAdapter

    private val suggestionModels = ArrayList<HistoryModel>()
    private var isSearchClicked = false
    private lateinit var adapter: CustomPagedAdapter
    private var updateHandler = Handler()
    private var gridLayoutManager = GridLayoutManager(this, 3)
    private var linearLayoutManager = LinearLayoutManager(this)

    private val adaptiveSearchHelper by lazy {
        AdaptiveSearchHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppThemeNoAction(this)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = " "

        binding.swipeRefreshLayout.isEnabled = false

        /** Hiding noMovieFound layout & initializing adaptive search
         */
        binding.activitySearchSingle.layoutNoMovieFound.hide()
        adaptiveSearchHelper.bindLayout(binding.activitySearchSingle.AdaptiveSearch)

        /** Setting suggestion RecyclerView and adapter with empty models
         */
        binding.suggestionRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestionAdapter = SearchAdapter(
            context = this,
            list = suggestionModels,
            onClick = { model, _ ->
                updateQuery(model.query)
            },
            onLongClick = { model, pos ->
                if (model.type == HistoryModel.Type.HISTORY)
                    showAlertAndDelete(model, pos)
            }
        )
        binding.suggestionRecyclerView.adapter = suggestionAdapter

        /** Removing recyclerview first to see the content view first
         */
        removeSuggestionRecyclerView()

        /** Setting close button onClickListener (shown beside editText in appBar)
         */
        binding.itemClose.setOnClickListener {
            binding.searchEditText.text.clear()
            it.hide()
        }

        binding.searchEditText.setOnEditorActionListener(onActionSearchEvent())

        binding.searchEditText.addTextChangedListener(searchEditTextChangeListener())

        setSuggestionObservable()
    }

    override fun onStart() {
        super.onStart()

        /** This focus will show the keyboard whenever this activity will be started
         */
        binding.searchEditText.requestFocus()

        /** Hiding the close button at start
         */
        binding.itemClose.hide()
    }

    /** This will setup final RecyclerView which will show all query.
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
        binding.swipeRefreshLayout.isRefreshing = true

        /** Remove any existing suggestion layout.
         */
        binding.activitySearchSingle.addLayout.removeAllViews()

        /** Setting up static queryMap and base of MoreActivity.
         */
        queryMap = YTSQuery.ListMoviesBuilder().apply {
            setQuery(binding.searchEditText.text.toString())
        }.build()
        base = MovieBase.YTS

        Log.e(TAG, "=> Query: ${QueryConverter.fromMapToString(queryMap!!)}")

        adapter = CustomPagedAdapter(this, MovieBase.YTS)

        moreViewModel.itemPagedList?.observe(this, observer)

        /** Setting layout manager as grid layout at first we will update it
         *  to linear layout manager if there is only single match.
         */
        binding.activitySearchSingle.recyclerView.layoutManager = gridLayoutManager

        binding.activitySearchSingle.recyclerView.adapter = adapter

        updateHandler.postDelayed(updateTask, 1000)
    }

    /** This will be observing the live-data coming from DataSource.
     */
    private val observer = Observer<PagedList<MovieShort>?> {
        Log.e(TAG, "=> Called submit list")
        adapter.submitList(it)
    }

    /** A handler basically to hide Refreshing of swipeLayout but now
     *  serves more than the purpose.
     */
    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            try {
                if (binding.activitySearchSingle.recyclerView.adapter?.itemCount ?: 0 <= 0) {

                    /** A hack used to know if there are no results found
                     *  and certainly displaying noMovieFound layout.
                     */
                    if (INITIAL_QUERY_FETCHED) {
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.activitySearchSingle.layoutNoMovieFound.show()
                    } else
                        updateHandler.postDelayed(this, 1000)
                } else {

                    /** Query is successful and we've some results.
                     */

                    binding.swipeRefreshLayout.isRefreshing = false

                    /** If there is only one item i.e single match, then linear layout manager
                     *  will be used and item_search_single_main will be displayed.
                     *
                     *  @see CustomPagedAdapter
                     */
                    if (adapter.itemCount == 1) {
                        binding.activitySearchSingle.recyclerView.layoutManager =
                            linearLayoutManager
                    }

                    if (adapter.itemCount <= 10) {

                        /** This will show suggestion layout
                         */
                        adapter.currentList?.get(0)?.imdbCode?.let { imdbCode ->
                            finalViewModel.getSuggestions(imdbCode, SuggestionCallback(
                                onComplete = { movies, tag, isMoreAvailable ->
                                    val layout =
                                        CustomMovieLayout(
                                            this@SearchActivity,
                                            "Suggested"
                                        )
                                    layout.injectViewAt(binding.activitySearchSingle.addLayout)
                                    layout.setupCallbacks(
                                        movies,
                                        "${imdbCode}/similar",
                                        isMoreAvailable
                                    )
                                }
                            ))
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
            Toasty.error(this, getString(R.string.empty_query)).show()
            return
        }

        /** Saving raw query to history */
        searchViewModel.addToHistory(text)

        /** Get the adaptive query */
        val searchQuery = adaptiveSearchHelper.querySearch(text)

        /** We need to recreate pagination list */
        moreViewModel.buildNewConfig()

        /** Hiding noMovieFound layout if visible
         */
        binding.activitySearchSingle.layoutNoMovieFound.hide()

        /** Setting this boolean true because search was clicked */
        isSearchClicked = true

        binding.searchEditText.clearFocus()

        /** Autocomplete text in search EditText */
        binding.searchEditText.setText(searchQuery)

        /** Hide the keyboard after search was clicked */
        hideKeyboard(this)

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
        suggestionFetch = RxTextView.textChanges(binding.searchEditText)
            .skip(700, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(onTaskStarted(), onTaskError())
    }

    /** Whenever user press search button on keyboard we will updateQuery
     */
    private fun onActionSearchEvent() = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH)
            updateQuery(binding.searchEditText.text.toString())
        true
    }

    /** This watcher is used to show and hide close button based on
     *  text query in search EditText
     */
    private fun searchEditTextChangeListener() = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s?.toString()?.isNotEmpty() == true)
                binding.itemClose.show()
            else binding.itemClose.hide()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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
        binding.suggestionLayout.hide()
    }

    /** Method will show an alert dialog to delete the history item.
     */
    private fun showAlertAndDelete(model: HistoryModel, pos: Int) {
        AlertDialog.Builder(this)
            .setTitle(model.query)
            .setMessage(getString(R.string.remove_history))
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                searchViewModel.deleteFromHistory(model.query)
                suggestionModels.removeAt(pos)
                suggestionAdapter.notifyDataSetChanged()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    /** Since Consumers are basically an async threads. Here we are making
     *  call to api using OkHttp for returning suggestion Json.
     *
     *  We will filter it and update the recyclerView adapter using
     *  runOnUiThread coroutine call.
     */
    private fun onTaskStarted() = Consumer<CharSequence> { charSequence ->

        /** Here, apart from checking if editText is empty we also have a boolean
         *  which serves an important purpose.
         *
         *  So suppose user typed some query in searchEdit text and suggestions are displayed
         *  in suggestion RecyclerView which follows this next path from => val response
         *
         *  Now whenever user click on suggestion it auto-completes the searchEdit text by
         *  changing the selected item text with current one. But RxJava is still watching
         *  this change and process it as a new query and runs the sequence => val response
         *  which eventually displays suggestion RecyclerView event after hiding it.
         *
         *  Hence in order to prevent it we are using a boolean which will detect if autocomplete
         *  was performed. If yes it won't run next code.
         */
        if (charSequence.isEmpty() && !isSearchClicked) {
            /** Load last saved search history if exist else remove
             *  suggestion layout */
            searchViewModel.getLastSavedSearchHistory(5) { list ->
                if (list.isNotEmpty()) {
                    Log.e(TAG, "Loading previous history")
                    suggestionModels.clear()
                    suggestionModels.addAll(list.map {
                        HistoryModel(
                            query = it.query,
                            type = HistoryModel.Type.HISTORY
                        )
                    })
                    suggestionAdapter.notifyDataSetChanged()

                    binding.suggestionLayout.show()
                } else {
                    Log.e(TAG, "No previous search history")
                    removeSuggestionRecyclerView()
                }
            }
            return@Consumer
        } else if (charSequence.isEmpty() || isSearchClicked) {
            runOnUiThread {
                isSearchClicked = false
                removeSuggestionRecyclerView()
            }
            return@Consumer
        }

        /** Fetch suggestions here */
        /* val response = retrofitUtils.getHttpClient().newCall(
             Request.Builder()
                 .url("${SUGGESTION_URL}$charSequence")
                 .build()
         ).execute()*/

        try {
            suggestionModels.clear()
            when (AppInterface.SUGGESTION_SEARCH_TYPE) {
                SearchType.Google -> {
                    suggestionHelper.getGoogleSuggestions(charSequence)
                        .map { result ->
                            HistoryModel(
                                query = result,
                                type = HistoryModel.Type.SEARCH
                            )
                        }.let { models ->
                            suggestionModels.addAll(models)
                        }
                }
                SearchType.TMDB -> {
                    suggestionHelper.getTMDBSuggestions(charSequence)
                        .map { result ->
                            HistoryModel(
                                query = result,
                                type = HistoryModel.Type.SEARCH
                            )
                        }.let { models ->
                            suggestionModels.addAll(models)
                        }
                }
            }
            /* val json = response.body?.string()
             if (json?.isNotEmpty() == true) {
                 val jsonArray = JSONArray(json).getJSONArray(1)
                 for (i in 0 until jsonArray.length()) {
                     suggestionModels.add(
                         HistoryModel(
                             query = jsonArray.getString(i),
                             type = HistoryModel.Type.SEARCH
                         )
                     )
                 }
             }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }

        runOnUiThread {
            /** Showing the hidden suggestion Layout only if
             *  there are items to show.
             */
            if (suggestionModels.isNotEmpty())
                binding.suggestionLayout.show()

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
        if (binding.searchEditText.text.isNotEmpty()) {
            binding.itemClose.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (binding.suggestionLayout.isVisible) {
            binding.suggestionLayout.hide()
            hideKeyboard(this)
        } else
            super.onBackPressed()
    }

    override fun onDestroy() {
        /** Dispose the RxText watcher and remove handler callbacks
         *  for safer side.
         */
        if (::suggestionFetch.isInitialized)
            suggestionFetch.dispose()

        updateHandler.removeCallbacks(updateTask)

        super.onDestroy()
    }
}
