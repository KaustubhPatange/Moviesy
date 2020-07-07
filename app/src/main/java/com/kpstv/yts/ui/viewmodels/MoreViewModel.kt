package com.kpstv.yts.ui.viewmodels

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.kpstv.yts.AppInterface.Companion.MOVIE_FETCH_SIZE
import com.kpstv.yts.interfaces.api.TMdbPlaceholderApi
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.models.MovieShort
import com.kpstv.yts.ui.viewmodels.providers.CustomDataSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext

class MoreViewModel @ViewModelInject constructor(
    private val tMdbPlaceholderApi: TMdbPlaceholderApi,
    private val ytsPlaceholderApi: YTSPlaceholderApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** This view model will manage the live data change of movies.
     */
    var itemPagedList: LiveData<PagedList<MovieShort>>? = null
    private var liveDataSource: LiveData<PageKeyedDataSource<Int, MovieShort>>? = null

    /**
     * Recreate the source factory.
     */
    fun buildNewConfig() {
        val sourceFactory =
            CustomDataSourceFactory(context, tMdbPlaceholderApi, ytsPlaceholderApi)
        liveDataSource = sourceFactory.itemLiveDataSource

        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(MOVIE_FETCH_SIZE)
            .build()

        itemPagedList = LivePagedListBuilder(sourceFactory, config).build()
    }
}