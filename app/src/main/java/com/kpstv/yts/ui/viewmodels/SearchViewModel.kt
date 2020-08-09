package com.kpstv.yts.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.data.db.repository.HistoryRepository
import com.kpstv.yts.data.models.data.data_history

class SearchViewModel @ViewModelInject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    fun addToHistory(query: String) {
        historyRepository.insert(data_history.from(query))
    }

    fun getLastSavedSearchHistory(max: Int, onComplete: (List<data_history>) -> Unit) = Coroutines.io {
        val items = historyRepository.getRecentHistory(max)
        Coroutines.main { onComplete.invoke(items) }
    }

    fun deleteFromHistory(query: String) {
        historyRepository.remove(query)
    }
}