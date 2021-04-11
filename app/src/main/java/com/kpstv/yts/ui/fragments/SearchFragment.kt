package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.common_moviesy.extensions.utils.KeyboardUtils
import com.kpstv.navigation.KeyedFragment
import com.kpstv.navigation.SharedPayload
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.adapters.CustomPagedAdapter
import com.kpstv.yts.adapters.HistoryModel
import com.kpstv.yts.adapters.SearchAdapter
import com.kpstv.yts.data.CustomDataSource
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.Result
import com.kpstv.yts.databinding.ActivitySearchBinding
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.SuggestionCallback
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import com.kpstv.yts.ui.helpers.AdaptiveSearchHelper
import com.kpstv.yts.ui.viewmodels.FinalViewModel
import com.kpstv.yts.ui.viewmodels.MoreViewModel
import com.kpstv.yts.ui.viewmodels.SearchViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

// As it is implementation from the old model.

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchFragment : KeyedFragment(R.layout.activity_search) {
    private val binding by viewBinding(ActivitySearchBinding::bind)
    private val moreViewModel by viewModels<MoreViewModel>()
    private val finalViewModel by viewModels<FinalViewModel>()
    private val searchViewModel by viewModels<SearchViewModel>()
    private val navViewModel by activityViewModels<StartViewModel>()

    @Inject lateinit var retrofitUtils: RetrofitUtils

    private lateinit var suggestionAdapter: SearchAdapter
    private val suggestionModels = ArrayList<HistoryModel>()
    private var isSearchClicked = false
    private lateinit var adapter: CustomPagedAdapter
    private var updateHandler = Handler()

    private val TAG = javaClass.simpleName

    private val gridLayoutManager by lazy { GridLayoutManager(requireContext(), 3) } // Calculate it somehow
    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) } // Calculate it somehow
    private val adaptiveSearchHelper by lazy {
        AdaptiveSearchHelper(requireContext(), viewLifecycleOwner)
    }

    companion object {
        fun createSharedPayload(fromView: View) = SharedPayload(
            mapOf(fromView to R.id.appbarLayout)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
        binding.swipeRefreshLayout.isEnabled = false

        /** Hiding noMovieFound layout & initializing adaptive search
         */
        binding.activitySearchSingle.layoutNoMovieFound.hide()
        adaptiveSearchHelper.bindLayout(binding.activitySearchSingle.AdaptiveSearch)

        /** Setting suggestion RecyclerView and adapter with empty models
         */
        binding.suggestionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        suggestionAdapter = SearchAdapter(
            context = requireContext(),
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

    private fun setToolbar() {
        binding.appbarLayout.applyTopInsets()
        binding.toolbar.title = " "
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { goBack() }
    }

    /** This will update the recyclerView following certain events as well.
     */
    private fun updateQuery(text: String) {
        Log.e(TAG, "Updating: $text")

        if (text.isEmpty()) {
            Toasty.error(requireContext(), getString(R.string.empty_query)).show()
            return
        }

        binding.activitySearchSingle.root.enableDelayedTransition()

        /** Saving raw query to history */
        searchViewModel.addToHistory(text)

        /** Get the adaptive query */
        val searchQuery = if (AppInterface.IS_ADAPTIVE_SEARCH)
            adaptiveSearchHelper.querySearch(text)
        else text

        /** We need to recreate pagination list */
        val queryMap = YTSQuery.ListMoviesBuilder().apply {
            setQuery(text)
        }.build()
        moreViewModel.buildNewConfig(base = MovieBase.YTS, queryMap = queryMap)

        /** Hiding noMovieFound layout if visible
         */
        binding.activitySearchSingle.layoutNoMovieFound.hide()

        /** Setting this boolean true because search was clicked */
        isSearchClicked = true

        binding.searchEditText.clearFocus()

        /** Autocomplete text in search EditText */
        binding.searchEditText.setText(searchQuery)

        /** Hide the keyboard after search was clicked */
        KeyboardUtils.hideKeyboard(requireContext())

        removeSuggestionRecyclerView()

        setupRecyclerView()
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

        adapter = CustomPagedAdapter(navViewModel, MovieBase.YTS)

        moreViewModel.itemPagedList?.observe(viewLifecycleOwner, observer)

        /** Setting layout manager as grid layout at first we will update it
         *  to linear layout manager if there is only single match.
         */
        binding.activitySearchSingle.recyclerView.layoutManager = gridLayoutManager

        binding.activitySearchSingle.recyclerView.adapter = adapter

        updateHandler.postDelayed(updateTask, 1000)
    }

    private val observer = Observer<PagedList<MovieShort>> {
        Log.e(TAG, "=> Called submit list")
        adapter.submitList(it)
        binding.searchEditText.hideKeyboard()
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
                    if (CustomDataSource.INITIAL_QUERY_FETCHED) {
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
                        binding.activitySearchSingle.recyclerView.layoutManager = linearLayoutManager
                    }

                    if (adapter.itemCount <= 10) {

                        /** This will show suggestion layout
                         */
                        adapter.currentList?.get(0)?.imdbCode?.let { imdbCode ->
                            finalViewModel.getSuggestions(imdbCode, SuggestionCallback(
                                onComplete = { movies, tag, isMoreAvailable ->
                                    val layout = CustomMovieLayout(requireContext(),"Suggested")
                                    layout.injectViewAt(binding.activitySearchSingle.addLayout)
                                    layout.setupCallbacks(
                                        navViewModel,
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
                e.printStackTrace()
                Toasty.error(requireContext(), "Error: ${e.message}").show()
            }
        }
    }

    /** Method will show an alert dialog to delete the history item.
     */
    private fun showAlertAndDelete(model: HistoryModel, pos: Int) {
        AlertNoIconDialog.Companion.Builder(requireContext())
            .setTitle(model.query)
            .setMessage(getString(R.string.remove_history))
            .setPositiveButton(getString(R.string.remove)) {
                searchViewModel.deleteFromHistory(model.query)
                suggestionModels.removeAt(pos)
                suggestionAdapter.notifyDataSetChanged()
            }
            .setNegativeButton(getString(R.string.cancel)) { }
            .show()
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

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.e(TAG, "Text Changed: $s")
            searchViewModel.setQuery(s.toString(), AppInterface.SUGGESTION_SEARCH_TYPE)
        }
    }

    /** This will be used to observe suggestions and based on [Result]
     *  suggestions are updated and shown.
     */
    private fun setSuggestionObservable() {
        searchViewModel.searchResults.observe(viewLifecycleOwner, Observer { result ->
            suggestionModels.clear()
            Log.e(TAG, "Emitting result $result")
            when (result) {
                is Result.Empty -> {
                    Log.e(TAG, "No previous search history")
                    removeSuggestionRecyclerView()
                }
                is Result.Success -> {
                    if (!isSearchClicked) {
                        suggestionModels.addAll(result.data)
                        if (suggestionModels.isNotEmpty())
                            binding.suggestionLayout.show()

                        suggestionAdapter.notifyDataSetChanged()
                    }
                }
                else -> { }
            }
            isSearchClicked = false
        })
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

    override fun onBackPressed(): Boolean {
        return if (binding.suggestionLayout.isVisible) {
            binding.suggestionLayout.hide()
            binding.searchEditText.hideKeyboard()
            true
        } else {
            binding.searchEditText.hideKeyboard()
            super.onBackPressed()
        }
    }

    override fun onDestroyView() {
        moreViewModel.itemPagedList?.removeObserver(observer)
        updateHandler.removeCallbacks(updateTask)
        super.onDestroyView()
    }
}