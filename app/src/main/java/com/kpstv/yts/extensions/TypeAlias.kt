package com.kpstv.yts.extensions

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.kpstv.yts.adapters.HistoryModel
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.TmDbCastMovie
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.response.Model

typealias SessionCallback = (Model.response_download?, Int) -> Unit
typealias SimpleCallback = () -> Unit
typealias AccountCallback = (GoogleSignInAccount) -> Unit
typealias ExceptionCallback = (Exception) -> Unit
typealias WorkManagerCallback = (LiveData<WorkInfo>) -> Unit
typealias SearchResults = List<HistoryModel>

typealias MovieOnComplete = (movies: ArrayList<MovieShort>, queryMap: Map<String, String>, isMoreAvailable: Boolean) -> Unit

typealias AdapterOnSingleClick<T> = (T, Int) -> Unit

data class CastMoviesCallback(
    val onFailure: ExceptionCallback?,
    val onComplete: (results: List<TmDbCastMovie>) -> Unit
)

data class MoviesCallback(
    val onStarted: SimpleCallback? = null,
    val onComplete: MovieOnComplete,
    val onFailure: ExceptionCallback? = null
)

data class SuggestionCallback(
    val onStarted: SimpleCallback? = null,
    val onComplete: (movies: ArrayList<TmDbMovie>, tag: String?, isMoreAvailable: Boolean) -> Unit,
    val onFailure: ExceptionCallback? = null
)