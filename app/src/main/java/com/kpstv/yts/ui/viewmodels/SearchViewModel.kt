package com.kpstv.yts.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.repository.HistoryRepository
import com.kpstv.yts.data.models.Result
import com.kpstv.yts.data.models.data.data_history
import com.kpstv.yts.extensions.SearchResults
import com.kpstv.yts.extensions.SearchType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

@FlowPreview
@ExperimentalCoroutinesApi
class SearchViewModel @ViewModelInject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    /**
     * The current text for suggestions.
     */
    private val query = MutableStateFlow("")
    private var suggestionType: SearchType = SearchType.TMDB

    val searchQuery: LiveData<Result<SearchResults>> = query
        .debounce(700)
        .distinctUntilChanged()
        .flatMapLatest {
            historyRepository.getSearchResults(it, suggestionType)
        }
        .asLiveData()


    fun addToHistory(query: String) {
        historyRepository.insert(data_history.from(query))
    }

    fun deleteFromHistory(query: String) {
        historyRepository.remove(query)
    }

    /**
     * Set the new [text] to search.
     */
    fun setQuery(text: String, searchType: SearchType) {
        query.value = text
        this.suggestionType = searchType
    }
}