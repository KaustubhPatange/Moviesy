package com.kpstv.yts.ui.viewmodels.providers

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.kpstv.yts.data.CustomDataSource
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.ui.activities.MoreActivity.Companion.base
import com.kpstv.yts.ui.activities.MoreActivity.Companion.endPoint
import com.kpstv.yts.ui.activities.MoreActivity.Companion.queryMap

class CustomDataSourceFactory(
    private val context: Context,
    private val tMdbApi: TMdbApi,
    private val ytsApi: YTSApi
): DataSource.Factory<Int,MovieShort>() {

    val itemLiveDataSource: MutableLiveData<PageKeyedDataSource<Int, MovieShort>> = MutableLiveData()

    override fun create(): DataSource<Int, MovieShort> {
        val customDataSource = CustomDataSource(
            context,
            tMdbApi,
            ytsApi
        )
        when (base) {
            MovieBase.TMDB -> customDataSource.setTMdbMovieSource(endPoint, base)
            MovieBase.YTS -> customDataSource.setYtsMovieSource(queryMap, base)
        }
        itemLiveDataSource.postValue(customDataSource)
        return customDataSource
    }
}