package com.kpstv.yts.data

import com.kpstv.yts.data.models.Movie

sealed class MovieResult {
    data class Success(val data: Movie): MovieResult()
    data class Error(val exception: Exception): MovieResult()
}