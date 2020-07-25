package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kpstv.yts.AppInterface.Companion.MOVIE_ID
import com.kpstv.yts.AppInterface.Companion.handleRetrofitError
import com.kpstv.yts.AppInterface.Companion.setAppThemeNoAction
import com.kpstv.yts.R
import com.kpstv.yts.adapters.GenreAdapter
import com.kpstv.yts.data.converters.GenreEnumConverter
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.AppUtils.Companion.CafebarToast
import com.kpstv.yts.extensions.utils.CustomMovieLayout
import com.kpstv.yts.extensions.utils.GlideApp
import com.kpstv.yts.interfaces.listener.FavouriteListener
import com.kpstv.yts.interfaces.listener.MovieListener
import com.kpstv.yts.interfaces.listener.SuggestionListener
import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.ui.fragments.sheets.BottomSheetDownload
import com.kpstv.yts.ui.fragments.sheets.BottomSheetSubtitles
import com.kpstv.yts.ui.viewmodels.FinalViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_final.*
import kotlinx.android.synthetic.main.activity_final_content.*
import kotlinx.android.synthetic.main.activity_final_content.view.*
import kotlinx.android.synthetic.main.activity_final_previews.*


@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class FinalActivity : AppCompatActivity(), MovieListener {

    val TAG = "FinalActivity"

    private val viewModel by viewModels<FinalViewModel>()

    private lateinit var movie: Movie
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var subtitleFetch: Disposable
    private lateinit var player: YouTubePlayer
    private var fetchHere = false;
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppThemeNoAction(this)

        setContentView(R.layout.activity_final)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = " "

        val movieId = intent.extras?.get(MOVIE_ID)

        /** Initializing YouTube player instance */
        initializeYoutubePlayer()

        /** Getting movie details for movieId */
        when (movieId) {
            is String -> {
                /** Route TMDB movie id */
                viewModel.getMovieDetail(this, movieId)
            }
            is Int -> {
                /** Route YTS movie id */
                viewModel.getMovieDetail(this, movieId)
            }
            else -> {
                CafebarToast(this, getString(R.string.invalid_movieId))
            }
        }
    }

    override fun onStarted() {
        hideLayout()
        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.isRefreshing = true
    }

    override fun onFailure(e: Exception) {
        handleRetrofitError(this@FinalActivity, e)
        e.printStackTrace()
        Log.e(TAG, "Failed--> " + e.message)
    }

    override fun onCastFetched(casts: ArrayList<Cast>) {
        this.movie.cast = casts
        setSummary()
    }

    override fun onComplete(movie: Movie) {
        this.movie = movie

        setMovieMenu()

        loadData()

        setPreviews()

        loadSimilar()

        loadRecommendation()

        setContentButtons()
        swipeRefreshLayout.isRefreshing = false;
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_final_menu, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (::movie.isInitialized) {
            when (item.itemId) {
                R.id.action_subtitles -> {
                    val bottomSheetSubtitles =
                        BottomSheetSubtitles()
                    bottomSheetSubtitles.show(supportFragmentManager, movie.imdb_code)
                }
                R.id.action_favourite -> {
                    viewModel.toggleFavourite(object : FavouriteListener {
                        override fun onToggleFavourite(id: Int?) {
                            if (id == R.drawable.ic_favorite_yes)
                                Toasty.info(this@FinalActivity, getString(R.string.add_watchlist)).show()
                            menu?.getItem(0)?.icon =
                                ContextCompat.getDrawable(this@FinalActivity, id!!)
                        }

                        override fun isMovieFavourite(value: Boolean) {

                        }
                    }, movie)
                }
            }
        } else
            Toasty.error(applicationContext, getString(R.string.movie_not_loaded)).show()

        return super.onOptionsItemSelected(item)
    }

    /** This is used to set menu i.e to show favourite icon properly
     *  to the menu item based on if Favourite exist or not.
     */
    private fun setMovieMenu() {
        viewModel.isMovieFavourite(object : FavouriteListener {
            override fun onToggleFavourite(id: Int?) {} // Ignore AF...

            override fun isMovieFavourite(value: Boolean) {
                if (value) {
                    menu?.getItem(0)?.icon =
                        ContextCompat.getDrawable(this@FinalActivity, R.drawable.ic_favorite_yes)
                }
            }
        }, movie.id)
    }

    private fun loadData() {

        /** Set top data */
        af_title.text = movie.title
        af_subtitle.text = "${movie.language} ${AppUtils.getBulletSymbol()} ${movie.year}"
        af_imdb_button.text = "imdb ${movie.rating}"
        af_imdb_button.setOnClickListener {
            AppUtils.launchUrl(this@FinalActivity, AppUtils.getImdbUrl(movie.imdb_code))
        }
        af_pg_button.text = if (movie.mpa_rating.isEmpty()) "R" else movie.mpa_rating

        setSummary()

        /** Set recyclerview data */
        recyclerView_genre.layoutManager =
            LinearLayoutManager(this@FinalActivity, LinearLayoutManager.HORIZONTAL, false)
        genreAdapter = GenreAdapter(this@FinalActivity, movie.genres)
        genreAdapter.setOnClickListener(object : GenreAdapter.OnClickListener {
            override fun onClick(text: String, position: Int) {
                val genre = GenreEnumConverter.toGenrefromString(text)
                if (genre != null) {
                    val queryMap = YTSQuery.ListMoviesBuilder().apply {
                        setGenre(genre)
                        setOrderBy(YTSQuery.OrderBy.descending)
                    }.build()
                    CustomMovieLayout.invokeMoreFunction(
                        this@FinalActivity,
                        "Based on $text",
                        queryMap
                    )
                } else Toasty.error(applicationContext, "Genre $text does not exist").show()
            }
        })
        recyclerView_genre.adapter = genreAdapter

        afc_card.visibility = View.VISIBLE

    }

    private fun loadRecommendation() {

        val suggestionListener = object : SuggestionListener {
            override fun onStarted() {
            }

            override fun onComplete(
                movies: ArrayList<TmDbMovie>,
                tag: String?,
                isMoreAvailable: Boolean
            ) {
                val recommendLayout = CustomMovieLayout(this@FinalActivity, "Recommended")
                recommendLayout.injectViewAt(af_addLayout)
                recommendLayout.setupCallbacks(movies, "$tag/recommendations", isMoreAvailable)
            }

            override fun onFailure(e: Exception) {
                handleRetrofitError(this@FinalActivity, e)
                e.printStackTrace()
            }
        }

        viewModel.getRecommendations(suggestionListener, movie.imdb_code)
    }

    private fun loadSimilar() {

        val suggestionListener = object : SuggestionListener {
            override fun onStarted() {
            }

            override fun onComplete(
                movies: ArrayList<TmDbMovie>,
                tag: String?,
                isMoreAvailable: Boolean
            ) {
                val similarLayout = CustomMovieLayout(this@FinalActivity, "Suggested")
                similarLayout.injectViewAt(af_addLayout)
                similarLayout.setupCallbacks(movies, "${movie.imdb_code}/similar", isMoreAvailable)
            }

            override fun onFailure(e: Exception) {
                handleRetrofitError(this@FinalActivity, e)
                e.printStackTrace()
            }
        }

        viewModel.getSuggestions(suggestionListener, movie.imdb_code)
    }

    private fun setPreviews() {
        GlideApp.with(applicationContext).asBitmap().load(movie.background_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    af_yt_preview_image.setImageBitmap(resource)
                    Log.e(TAG, "Image Loaded")
                    af_yt_preview_image.setOnClickListener {
                        Log.e(TAG, "OnClicked")
                        if (::player.isInitialized) {
                            player.loadVideo(movie.yt_trailer_id, 0f)
                            youtube_player_view.visibility = View.VISIBLE
                        } else {
                            af_yt_preview_play.visibility = View.GONE
                            af_yt_preview_progressBar.visibility = View.VISIBLE
                        }
                    }

                    af_yt_preview.visibility = View.VISIBLE
                }
            })

        GlideApp.with(applicationContext).asBitmap().load(movie.medium_cover_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    shimmerFrame.hide()

                    af_yt_banner_image.setImageBitmap(resource)
                    af_yt_banner_image.visibility = View.VISIBLE
                    af_yt_banner_image.setOnClickListener {
                        val sharedImageView = findViewById<View>(R.id.af_yt_banner_image)
                        val intent = Intent(this@FinalActivity, ImageViewActivity::class.java)
                        intent.putExtra("imageUrl", movie.large_cover_image)
                        val options = ActivityOptions
                            .makeSceneTransitionAnimation(
                                this@FinalActivity,
                                sharedImageView,
                                "banner_photo"
                            )
                        startActivity(intent, options.toBundle())
                    }
                }
            })
        afp_scroll.visibility = View.VISIBLE
    }

    private fun setSummary() {
        val color = AppUtils.getColorFromAttr(this@FinalActivity, R.attr.colorText)

        af_summary.text = "${movie.description_full}\n\n"

        /** YTS sometimes doesn't add cast to its movie so we are checking it */

        if (!movie.cast.isNullOrEmpty()) {
            val builder = StringBuilder()
            for (i in 0 until movie.cast?.size!! -1) {
                builder.append(movie.cast?.get(i)?.name).append("  &#8226;  ")
            }
            builder.append(movie.cast?.get(movie.cast?.size!! - 1)?.name)

            /** If cast not empty add details it to summary */
            af_summary.append(AppUtils.getColoredString("<b>Starring</b>", color))
            af_summary.append(" ${AppUtils.getHtmlText(builder.toString())}\n\n")
        }

        af_summary.append(AppUtils.getColoredString("<b>Runtime</b>", color))
        af_summary.append(" ${movie.runtime} mins")


        af_moreTxt.af_moreTxt.setOnClickListener {
            if (af_moreTxt.text.toString() == "More") {
                af_moreTxt.text = "Less"
                af_summary.maxLines = Integer.MAX_VALUE;
            } else {
                af_moreTxt.text = "More"
                af_summary.maxLines = 3;
            }
        }
    }

    private fun setContentButtons() {
        af_download.setOnClickListener {
            loadBottomSheetDownload("download")
        }
        af_watch.setOnClickListener {
            loadBottomSheetDownload("watch_now")
        }
    }

    private fun loadBottomSheetDownload(tag: String) {
        val sheet = BottomSheetDownload()
        val bundle = Bundle();
        bundle.putSerializable("models", movie.torrents)
        bundle.putString("title", movie.title)
        bundle.putString("imdbCode", movie.imdb_code)
        bundle.putString("imageUri", movie.medium_cover_image)
        bundle.putInt("movieId", movie.id)
        sheet.arguments = bundle
        sheet.show(supportFragmentManager, tag)
    }

    private fun initializeYoutubePlayer() {
        youtube_player_view.enableAutomaticInitialization = false;
        youtube_player_view.initialize(object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {

            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {
            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate
            ) {
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
                Log.e(TAG, "YouTubePlayerInitialized")
                player = youTubePlayer
                af_yt_preview_progressBar.visibility = View.GONE
                af_yt_preview_play.visibility = View.VISIBLE

                button_fullscreen.setOnClickListener {
                    player.pause()
                    val i = Intent(this@FinalActivity, PlayerActivity::class.java)
                    i.putExtra("videoId", movie.yt_trailer_id)
                    startActivity(i)
                }
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> button_fullscreen.visibility =
                        View.VISIBLE
                    PlayerConstants.PlayerState.PAUSED -> button_fullscreen.visibility = View.GONE
                    PlayerConstants.PlayerState.ENDED -> {
                        af_yt_preview_play.visibility = View.VISIBLE
                        af_yt_preview_progressBar.visibility = View.GONE
                        youtube_player_view.visibility = View.GONE
                        button_fullscreen.visibility = View.GONE
                    }
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {
            }

        })
    }

    private fun hideLayout() {
        afc_card.visibility = View.GONE
        afp_scroll.visibility = View.GONE
        af_yt_preview.visibility = View.GONE
        af_yt_banner_image.visibility = View.GONE
        // afs_layout.visibility = View.GONE
    }

    override fun onDestroy() {

        if (::player.isInitialized) player.pause()
        youtube_player_view.release()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true;
    }


}

