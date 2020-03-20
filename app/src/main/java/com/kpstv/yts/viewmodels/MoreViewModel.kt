package com.kpstv.yts.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.kpstv.yts.AppInterface.Companion.MOVIE_FETCH_SIZE
import com.kpstv.yts.interfaces.api.TMdbPlaceholderApi
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.models.MovieShort
import com.kpstv.yts.viewmodels.providers.CustomDataSourceFactory

class MoreViewModel(
    private val tMdbPlaceholderApi: TMdbPlaceholderApi,
    private val ytsPlaceholderApi: YTSPlaceholderApi,
    application: Application
): AndroidViewModel(application) {

    /** This view model will manage the live data change of movies.
     */

    var itemPagedList: LiveData<PagedList<MovieShort>>? = null
    private var liveDataSource: LiveData<PageKeyedDataSource<Int, MovieShort>>? = null

     init {
         val sourceFactory = CustomDataSourceFactory(application, tMdbPlaceholderApi,ytsPlaceholderApi)
         liveDataSource = sourceFactory.itemLiveDataSource

         val config: PagedList.Config = PagedList.Config.Builder()
             .setEnablePlaceholders(false)
             .setPageSize(MOVIE_FETCH_SIZE)
             .build()

         itemPagedList = LivePagedListBuilder(sourceFactory, config).build()
     }
}