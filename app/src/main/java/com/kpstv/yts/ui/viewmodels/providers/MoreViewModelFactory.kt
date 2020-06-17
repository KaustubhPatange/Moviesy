package com.kpstv.yts.ui.viewmodels.providers

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kpstv.yts.interfaces.api.TMdbPlaceholderApi
import com.kpstv.yts.interfaces.api.YTSPlaceholderApi
import com.kpstv.yts.ui.viewmodels.MoreViewModel

class MoreViewModelFactory(
    private val application: Application,
    private val ytsPlaceholderApi: YTSPlaceholderApi,
    private val tMdbPlaceholderApi: TMdbPlaceholderApi
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MoreViewModel(tMdbPlaceholderApi,ytsPlaceholderApi,application) as T
    }
}