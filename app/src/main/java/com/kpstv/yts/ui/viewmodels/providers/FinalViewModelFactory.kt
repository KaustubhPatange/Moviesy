package com.kpstv.yts.ui.viewmodels.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.data.db.repository.MovieRepository
import com.kpstv.yts.data.db.repository.TMdbRepository
import com.kpstv.yts.interfaces.api.TMdbPlaceholderApi
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.ui.viewmodels.FinalViewModel

class FinalViewModelFactory(
    private val movieRepo: MovieRepository,
    private val tMdbRepo: TMdbRepository,
    private val ytsPlaceholderApi: YTSPlaceholderApi,
    private val tMdbPlaceholderApi: TMdbPlaceholderApi,
    private val favouriteRepository: FavouriteRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FinalViewModel(
            movieRepo,
            tMdbRepo,
            ytsPlaceholderApi,
            tMdbPlaceholderApi,
            favouriteRepository
        ) as T
    }
}