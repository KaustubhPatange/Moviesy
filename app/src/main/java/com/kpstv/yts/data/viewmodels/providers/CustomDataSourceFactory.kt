package com.kpstv.yts.data.viewmodels.providers

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.kpstv.yts.ui.activities.MoreActivity.Companion.base
import com.kpstv.yts.ui.activities.MoreActivity.Companion.endPoint
import com.kpstv.yts.ui.activities.MoreActivity.Companion.queryMap
import com.kpstv.yts.data.CustomDataSource
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.interfaces.api.TMdbPlaceholderApi
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.models.MovieShort

class CustomDataSourceFactory(
    private val application: Application,
    private val tMdbPlaceholderApi: TMdbPlaceholderApi,
    private val ytsPlaceholderApi: YTSPlaceholderApi
): DataSource.Factory<Int,MovieShort>() {

    val itemLiveDataSource: MutableLiveData<PageKeyedDataSource<Int, MovieShort>> = MutableLiveData()

    override fun create(): DataSource<Int, MovieShort> {
        val customDataSource = CustomDataSource(
            application,
            tMdbPlaceholderApi,
            ytsPlaceholderApi
        )
        when (base) {
            MovieBase.TMDB -> customDataSource.setTMdbMovieSource(endPoint, base)
            MovieBase.YTS -> customDataSource.setYtsMovieSource(queryMap, base)
        }
        itemLiveDataSource.postValue(customDataSource)
        return customDataSource
    }
}