package com.kpstv.yts.ui.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.MOVIE_SPAN_DIFFERENCE
import com.kpstv.yts.AppInterface.Companion.MainDateFormatter
import com.kpstv.yts.data.MovieResult
import com.kpstv.yts.data.db.localized.MovieDao
import com.kpstv.yts.data.db.localized.RecommendDao
import com.kpstv.yts.data.db.localized.SuggestionDao
import com.kpstv.yts.data.db.repository.CastMovieRepository
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Crew
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.data.data_tmdb
import com.kpstv.yts.extensions.*
import com.kpstv.yts.extensions.errors.MovieNotFoundException
import com.kpstv.yts.extensions.utils.YTSParser
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.interfaces.listener.MovieListener
import kotlinx.coroutines.launch
import retrofit2.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SimpleDateFormat")
class FinalViewModel @ViewModelInject constructor(
    private val movieRepository: MovieDao,
    private val suggestionDao: SuggestionDao,
    private val recommendDao: RecommendDao,
    private val favouriteRepository: FavouriteRepository,
    private val castMovieRepository: CastMovieRepository,
    private val ytsParser: YTSParser,
    private val ytsApi: YTSApi,
    private val tMdbApi: TMdbApi
) : ViewModel() {

    private val TAG = "FinalViewModel"

    fun isMovieFavourite(movieId: Int) = favouriteRepository.isMovieFavouriteLive(movieId)

    fun toggleFavourite(movie: Movie, onFavouriteMarked: SimpleCallback) {
        viewModelScope.launch {
            if (favouriteRepository.toggleFavourite(movie))
                onFavouriteMarked.invoke()
        }
    }

    fun fetchMovieUrl(movieListener: MovieListener, movieUrl: String) {
        movieListener.onStarted()
        viewModelScope.launch {
            val movieShort = ytsParser.parseMovieUrl(movieUrl)
            if (movieShort?.movieId != null) {
                val movieResult = getMovieDetailYTS(movieShort.movieId)
                when(movieResult) {
                    is MovieResult.Success -> {
                        movieListener.onComplete(movieResult.data)
                    }
                    is MovieResult.Error -> {
                        movieListener.onFailure(movieResult.exception)
                    }
                }
            } else {
                movieListener.onFailure(Exception("Failed to fetch the movie"))
            }
        }
    }

    fun getMovieDetailYTS(movieListener: MovieListener, movieId: Int) {
        movieListener.onStarted()

        viewModelScope.launch {
            val movieResult = getMovieDetailYTS(movieId)
            when(movieResult) {
                is MovieResult.Success -> {
                    movieListener.onComplete(movieResult.data)
                }
                is MovieResult.Error -> {
                    movieListener.onFailure(movieResult.exception)
                }
            }
        }
    }

    private suspend fun getMovieDetailYTS(movieId: Int): MovieResult {
        val query = YTSQuery.MovieBuilder()
            .setMovieId(movieId)
            .build()

        try {
            val response = ytsApi.getMovie(query)
            val movie = response?.data?.movie ?: return MovieResult.Error(Exception("Couldn't find the required movie."))

            /** Fetching crew details filtering only director */
            val crews = movieRepository.getCrewById(movieId)
            val casts = movieRepository.getCastById(movieId)
            if (crews == null || casts == null) {
                injectCastInfo(movie)
            } else {
                movie.crew = crews
                movie.cast = casts
            }
            movieRepository.saveMovie(movie)
            return MovieResult.Success(movie)
        } catch (e: Exception) {
            e.printStackTrace()
            return movieRepository.getMovieById(movieId)?.let { movie ->
                MovieResult.Success(movie)
            } ?: MovieResult.Error(e)
        }
    }

    fun getMovieDetailTMdb(movieListener: MovieListener, queryString: String) {
        movieListener.onStarted()

        viewModelScope.launch {
            try {

                /** Since the queryString is nothing but a TMDB Movie ID
                 * so we first need to get details of the TMDB movie to
                 * extract IMDB Id.
                 */
                val responseMovie = tMdbApi.getMovie(queryString)

                val query = YTSQuery.ListMoviesBuilder().apply {
                    setQuery(responseMovie.imdbCode)
                    setLimit(1)
                }.build()

                /** Using IMDB Id we can query the ytsApi to filter movies
                 * and get the movie we wanted.
                 */
                val response = ytsApi.listMovies(query).await()
                if (response.data.movie_count > 0) {
                    val movie = response.data.movies?.get(0)!!
                    movieListener.onComplete(movie)

                    /** A patch that will modify movie and inject cast & director
                     */
                    injectCastInfo(movie)
                    if (movie.cast != null && movie.crew != null) {
                        movieListener.onCastFetched(movie.cast!!, movie.crew!!)
                    }

                    movieRepository.saveMovie(movie)
                } else movieListener.onFailure(MovieNotFoundException())
            } catch (e: Exception) {
                movieRepository.getMovieByTitleLong(queryString)?.let {
                    movieListener.onComplete(it)
                }
                movieListener.onFailure(e)
            }
        }
    }

    private suspend fun injectCastInfo(movie: Movie) {
        try {
            val tmDbResponse = tMdbApi.getCast(movie.imdb_code)
            if (tmDbResponse.crew != null && tmDbResponse.cast != null) {
                val casts = tmDbResponse.cast.take(4).map { Cast.from(it) }
                val crewList = tmDbResponse.crew.filter { it.job == "Director" }.take(3).map { Crew.from(it) }
                movie.cast = casts
                movie.crew = crewList
            }
        }catch (e: Exception) {
            // low-level exception can be ignored.
        }
    }

    fun getRecommendations(imdbId: String, suggestionCallback: SuggestionCallback) {
        suggestionCallback.onStarted?.invoke()
        viewModelScope.launch {
            try {

                if (isFetchNeeded(imdbId, MovieType.Recommend)) {
                    Log.e(TAG, "=> Fetching New data")

                    /** Since Recommendation query needs only TMDB movie id,
                     *  we first get the movie from tMdbApi to fetch the movie id
                     */
                    val response = tMdbApi.getMovie(imdbId)

                    /** Now we will fetch the recommendations using the movie id
                     */
                    val response1 = tMdbApi.getRecommendations(response.movieId)

                    commonProcessTMdbMovies(
                        suggestionCallback,
                        response1.results,
                        imdbId,
                        MovieType.Recommend,
                        response.movieId.toString()
                    )
                } else {
                    Log.e(TAG, "=> Getting data from repository")

                    recommendDao.getMoviesByImDb(imdbId)?.also {
                        suggestionCallback.onComplete(it.movies, it.tag, it.isMore)
                    }
                }
            } catch (e: Exception) {
                suggestionCallback.onFailure?.invoke(e)
            }
        }
    }

    fun getSuggestions(imdbId: String, suggestionCallback: SuggestionCallback) {
        suggestionCallback.onStarted?.invoke()

        viewModelScope.launch {
            try {
                if (isFetchNeeded(imdbId, MovieType.Suggestion)) {
                    Log.e(TAG, "=> Fetching New data")

                    val response = tMdbApi.getSimilar(imdbId)

                    commonProcessTMdbMovies(
                        suggestionCallback,
                        response.results,
                        imdbId,
                        MovieType.Suggestion
                    )
                } else {
                    Log.e(TAG, "=> Getting data from repository")

                    suggestionDao.getMoviesByImDb(imdbId)?.also {
                        suggestionCallback.onComplete(it.movies, it.tag, it.isMore)
                    }
                }
            } catch (e: Exception) {
                suggestionCallback.onFailure?.invoke(e)
            }
        }
    }

    /** This will return two of the top cast starring movies with opposite gender.
     *  Behavior: It will take 5 top cast crew and find two opposite gender person
     *  and return there movies.
     */
    fun getTopCrewMovies(imdbCode: String, listener: CastMoviesCallback) {
        viewModelScope.launch {
            try {
                val results = castMovieRepository.fetchResults(imdbCode)
                listener.onComplete.invoke(results)
            } catch (e: Exception) {
                listener.onFailure?.invoke(e)
            }
        }
    }

    /** This function will be used to check if we need to update the suggestion
     *  or recommend data.
     *
     *  If true then we will contact our server to fetch the data online otherwise
     *  we'll fetch it from database itself.
     */
    private suspend fun isFetchNeeded(imdbId: String, movieType: MovieType): Boolean {
        try {
            val movieData = when (movieType) {
                MovieType.Suggestion -> suggestionDao.getMoviesByImDb(imdbId)
                MovieType.Recommend -> recommendDao.getMoviesByImDb(imdbId)
            }
            movieData?.also {
                val currentCalender = Calendar.getInstance()
                currentCalender.add(Calendar.HOUR, -AppInterface.QUERY_SPAN_DIFFERENCE)
                val currentSpan = MainDateFormatter.format(
                    currentCalender.time
                ).toLong()
                return if (currentSpan > movieData.time) {
                    when (movieType) {
                        MovieType.Suggestion -> suggestionDao.deleteMovies(movieData)
                        MovieType.Recommend -> recommendDao.deleteMovies(movieData)
                    }
                    true
                } else false
            }
            return true
        } catch (e: Exception) {
            return true
        }
    }

    private fun commonProcessTMdbMovies(
        suggestionCallback: SuggestionCallback,
        movies: ArrayList<TmDbMovie>,
        imdbId: String,
        movieType: MovieType,
        tag: String? = null
    ) {
        viewModelScope.launch {
            val list = ArrayList<TmDbMovie>()
            manageTmdbResponse(list, movies)

            var toIndex = 7
            if (list.size < 8) toIndex = list.size

            val isMore = list.size > 7

            val movieList = ArrayList(list.subList(0, toIndex))

            /** We will save the data into TMdb database as per movieType */

            val data = data_tmdb(
                imdbCode = imdbId,
                time = MainDateFormatter.format(Calendar.getInstance().time).toLong(),
                movies = movieList,
                isMore = isMore,
                tag = tag
            )

            when (movieType) {
                MovieType.Suggestion -> suggestionDao.saveMovies(data)
                MovieType.Recommend -> recommendDao.saveMovies(data)
            }

            suggestionCallback.onComplete.invoke(
                movieList,
                tag,
                isMore
            )
        }
    }


    /** This piece of code manages the TMDB movies by filter their release date
     *  to be ${MOVIE_SPAN_DIFFERENCE} months older than actual.
     *
     *  This is done since YTS uploads movie whenever their DVD comes out,
     *  usually DVD comes out after 3 months after official release.
     *
     *  @Warning Some movies are released on YTS before this above span due to this
     *           they are not shown, currently there is no fix for this rather than
     *           manually searching it.
     */
    private fun manageTmdbResponse(
        listToModify: ArrayList<TmDbMovie>,
        results: ArrayList<TmDbMovie>
    ) {
        listToModify.clear()
        results.forEach { movie ->

            /** There are some movies in TMDB where release data object does not exist.
             *  Hence we've to make a null check.
             */

            if (!movie.release_date.isNullOrBlank()) {
                if (SimpleDateFormat("yyyy-MM-dd").parse(movie.release_date)?.let { date ->
                        Calendar.getInstance().apply {
                            time = date
                            add(Calendar.MONTH, -MOVIE_SPAN_DIFFERENCE)
                        }.let { calender ->
                            Calendar.getInstance() > calender
                        }
                    }!!) {
                    listToModify.add(movie)
                }
            }
        }
    }
}