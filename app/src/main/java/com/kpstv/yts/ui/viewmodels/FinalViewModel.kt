package com.kpstv.yts.ui.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppInterface.Companion.MOVIE_SPAN_DIFFERENCE
import com.kpstv.yts.AppInterface.Companion.MainDateFormatter
import com.kpstv.yts.AppInterface.Companion.TMDB_IMAGE_PREFIX
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.data.db.repository.FavouriteRepository
import com.kpstv.yts.data.db.repository.MovieRepository
import com.kpstv.yts.data.db.repository.TMdbRepository
import com.kpstv.yts.extensions.Coroutines
import com.kpstv.yts.extensions.MovieType
import com.kpstv.yts.interfaces.api.TMdbApi
import com.kpstv.yts.interfaces.api.YTSApi
import com.kpstv.yts.interfaces.listener.FavouriteListener
import com.kpstv.yts.interfaces.listener.MovieListener
import com.kpstv.yts.interfaces.listener.SuggestionListener
import com.kpstv.yts.models.Cast
import com.kpstv.yts.models.Movie
import com.kpstv.yts.models.TmDbMovie
import com.kpstv.yts.models.data.data_tmdb
import com.kpstv.yts.extensions.utils.AppUtils
import retrofit2.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList

@SuppressLint("SimpleDateFormat")
class FinalViewModel @ViewModelInject constructor(
    private val movieRepository: MovieRepository,
    private val tMdbRepository: TMdbRepository,
    private val ytsApi: YTSApi,
    private val tMdbApi: TMdbApi,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    @Volatile
    private var isSuggestedShown = false
    private val lock = ReentrantLock()
    private val TAG = "FinalViewModel"

    fun isMovieFavourite(listener: FavouriteListener, movieId: Int) {
        Coroutines.main {
            listener.isMovieFavourite(AppUtils.isMovieFavourite(favouriteRepository, movieId))
        }
    }

    fun toggleFavourite(listener: FavouriteListener, movie: Movie){
        Coroutines.main {
            listener.onToggleFavourite(AppUtils.toggleFavourite(favouriteRepository, movie))
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
                movieRepository.getMovieById(movieId)?.let {
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
                val responseMovie = tMdbApi.getMovie(queryString).await()

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
                    val response1 = tMdbApi.getCast(movie.imdb_code).await()
                    if (response1.cast?.isNotEmpty()!!) {
                        val list = ArrayList<Cast>()
                        response1.cast.forEach {
                            if (list.size < 4) {
                                list.add(
                                    Cast(
                                        it.name, it.character,
                                        "${TMDB_IMAGE_PREFIX}${it.profilePath}",
                                        it.id.toString()
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

    fun getRecommendations(suggestionListener: SuggestionListener, imdbId: String) {
        suggestionListener.onStarted()

        Coroutines.main {
            try {

                if (isFetchNeeded(imdbId, MovieType.Recommend)) {
                    Log.e(TAG, "==> Fetching New data")

                    /** Since Recommendation query needs only TMDB movie id,
                     *  we first get the movie from tMdbApi to fetch the movie id
                     */
                    val response = tMdbApi.getMovie(imdbId).await()

                    /** Now we will fetch the recommendations using the movie id
                     */
                    val response1 = tMdbApi.getRecommendations(response.id).await()

                    commonProcessTMdbMovies(
                        suggestionListener,
                        response1.results,
                        imdbId,
                        MovieType.Recommend,
                        response.id.toString()
                    )
                } else {
                    Log.e(TAG, "==> Getting data from repository")

                    tMdbRepository.getRecommendMoviesByIMDB(imdbId)?.also {
                        suggestionListener.onComplete(it.movies, it.tag, it.isMore)
                    }
                }
            } catch (e: Exception) {
                suggestionListener.onFailure(e)
            }
        }
    }

    fun getSuggestions(suggestionListener: SuggestionListener, imdbId: String) {
        suggestionListener.onStarted()

        Coroutines.main {
            try {
                if (isFetchNeeded(imdbId, MovieType.Suggestion)) {
                    Log.e(TAG, "==> Fetching New data")

                    val response = tMdbApi.getSimilars(imdbId).await()

                    commonProcessTMdbMovies(
                        suggestionListener,
                        response.results,
                        imdbId,
                        MovieType.Suggestion
                    )
                } else {
                    Log.e(TAG, "==> Getting data from repository")

                    tMdbRepository.getSuggestMoviesByIMDB(imdbId)?.also {
                        suggestionListener.onComplete(it.movies, it.tag, it.isMore)
                    }
                }
                isSuggestedShown = true
            } catch (e: Exception) {
                suggestionListener.onFailure(e)
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
    @Synchronized private fun commonProcessTMdbMovies(
        suggestionListener: SuggestionListener,
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

        suggestionListener.onComplete(
            movies = movieList,
            isMoreAvailable = isMore,
            tag = tag
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