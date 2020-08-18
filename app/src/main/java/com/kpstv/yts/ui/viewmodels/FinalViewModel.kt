package com.kpstv.yts.ui.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.MOVIE_SPAN_DIFFERENCE
import com.kpstv.yts.AppInterface.Companion.MainDateFormatter
import com.kpstv.yts.AppInterface.Companion.TMDB_IMAGE_PREFIX
import com.kpstv.yts.data.db.repository.CastMovieRepository
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.data.db.repository.MovieRepository
import com.kpstv.yts.data.db.repository.TMdbRepository
import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.data.models.data.data_tmdb
import com.kpstv.yts.extensions.*
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
    private val movieRepository: MovieRepository,
    private val tMdbRepository: TMdbRepository,
    private val favouriteRepository: FavouriteRepository,
    private val castMovieRepository: CastMovieRepository,
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

    fun getMovieDetail(movieListener: MovieListener, movieId: Int) {
        movieListener.onStarted()

        val query = YTSQuery.MovieBuilder()
            .setMovieId(movieId)
            .setIncludeCast(true)
            .build()

        Coroutines.main {
            try {
                val response = ytsApi.getMovie(query).await()
                val movie = response.data.movie
                movieRepository.saveMovie(movie!!)
                movieListener.onComplete(movie)
            } catch (e: Exception) {
                movieRepository.getMovieById(movieId).let {
                    movieListener.onComplete(it)
                }
                movieListener.onFailure(e)
            }
        }
    }

    fun getMovieDetail(movieListener: MovieListener, queryString: String) {
        movieListener.onStarted()

        Coroutines.main {
            try {

                /** Since the queryString is nothing but a TMDB Movie ID
                 * so we first need to get details of the TMDB movie to
                 * extract IMDB Id.
                 */
                val responseMovie = tMdbApi.getMovie(queryString)

                val query = YTSQuery.ListMoviesBuilder().apply {
                    setQuery(responseMovie.imdb_id)
                    setLimit(1)
                }.build()

                /** Using IMDB Id we can query the ytsApi to filter movies
                 * and get the movie we wanted.
                 */
                val response = ytsApi.listMovies(query).await()
                if (response.data.movie_count > 0) {
                    val movie = response.data.movies?.get(0)!!
                    movieListener.onComplete(movie)

                    /** A patch that will modify movie and inject cast
                     */
                    val response1 = tMdbApi.getCast(movie.imdb_code)
                    if (response1.cast?.isNotEmpty() == true) {
                        val list = ArrayList<Cast>()
                        response1.cast.forEach {
                            if (list.size < 4) {
                                list.add(
                                    Cast(
                                        it.name, it.character,
                                        "${TMDB_IMAGE_PREFIX}${it.profilePath}",
                                        it.personId.toString()
                                    )
                                )
                            } else return@forEach
                        }
                        movie.cast = list
                        movieListener.onCastFetched(list)
                    }

                    /** This will save the movie in our SQLite database
                     */
                    movieRepository.saveMovie(movie)
                } else movieListener.onFailure(Exception("Given movie does not exist in database"))
            } catch (e: Exception) {
                movieRepository.getMovieByTitleLong(queryString)?.let {
                    movieListener.onComplete(it)
                }
                movieListener.onFailure(e)
            }
        }
    }

    fun getRecommendations(imdbId: String, suggestionCallback: SuggestionCallback) {
        suggestionCallback.onStarted?.invoke()
        Coroutines.main {
            try {

                if (isFetchNeeded(imdbId, MovieType.Recommend)) {
                    Log.e(TAG, "=> Fetching New data")

                    /** Since Recommendation query needs only TMDB movie id,
                     *  we first get the movie from tMdbApi to fetch the movie id
                     */
                    val response = tMdbApi.getMovie(imdbId)

                    /** Now we will fetch the recommendations using the movie id
                     */
                    val response1 = tMdbApi.getRecommendations(response.id)

                    commonProcessTMdbMovies(
                        suggestionCallback,
                        response1.results,
                        imdbId,
                        MovieType.Recommend,
                        response.id.toString()
                    )
                } else {
                    Log.e(TAG, "=> Getting data from repository")

                    tMdbRepository.getRecommendMoviesByIMDB(imdbId)?.also {
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

        Coroutines.main {
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

                    tMdbRepository.getSuggestMoviesByIMDB(imdbId)?.also {
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
            }catch (e: Exception) {
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
                MovieType.Suggestion -> tMdbRepository.getSuggestMoviesByIMDB(imdbId)
                MovieType.Recommend -> tMdbRepository.getRecommendMoviesByIMDB(imdbId)
            }
            movieData?.also {
                val currentCalender = Calendar.getInstance()
                currentCalender.add(Calendar.HOUR, -AppInterface.QUERY_SPAN_DIFFERENCE)
                val currentSpan = MainDateFormatter.format(
                    currentCalender.time
                ).toLong()
                return if (currentSpan > movieData.time) {
                    when (movieType) {
                        MovieType.Suggestion -> tMdbRepository.deleteSuggestMovieModel(movieData)
                        MovieType.Recommend -> tMdbRepository.deleteRecommendMovieModel(movieData)
                    }
                    true
                } else false
            }
            return true
        } catch (e: Exception) {
            return true
        }
    }

    /** A common processing function for managing Suggestion and Recommend data list.
     *  The function is also responsible for saving data to database.
     */
    @Synchronized
    private fun commonProcessTMdbMovies(
        suggestionCallback: SuggestionCallback,
        movies: ArrayList<TmDbMovie>,
        imdbId: String,
        movieType: MovieType,
        tag: String? = null
    ) {
        val list = ArrayList<TmDbMovie>()
        manageTmdbResponse(list, movies)

        var toIndex = 7
        if (list.size < 8) toIndex = list.size

        val isMore = list.size > 7

        val movieList = ArrayList(list.subList(0, toIndex))

        suggestionCallback.onComplete.invoke(
            movieList,
            tag,
            isMore
        )

        /** We will save the data into TMdb database as per movieType
         */

        val data = data_tmdb(
            imdbCode = imdbId,
            time = MainDateFormatter.format(Calendar.getInstance().time).toLong(),
            movies = movieList,
            isMore = isMore,
            tag = tag
        )

        when (movieType) {
            MovieType.Suggestion -> tMdbRepository.saveSuggestMoviesModel(data)
            MovieType.Recommend -> tMdbRepository.saveRecommendMoviesModel(data)
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