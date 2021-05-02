package com.kpstv.yts.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.yts.data.db.localized.DownloadDao
import com.kpstv.yts.data.models.response.Model
import kotlinx.coroutines.launch

class LibraryViewModel @ViewModelInject constructor(
    private val downloadDao: DownloadDao
) : ViewModel() {

    private val _selectedMovieId: MutableLiveData<Model.response_download?> = MutableLiveData(null)
    val selectedMovieId: LiveData<Model.response_download?> = _selectedMovieId

    fun selectMovie(movieId: Int) {
        viewModelScope.launch {
            val model = downloadDao.getDownload(movieId = movieId)
            _selectedMovieId.value = model
            _selectedMovieId.value = null
        }
    }
}