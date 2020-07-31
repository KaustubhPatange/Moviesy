package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
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
import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.data.models.TmDbMovie
import com.kpstv.yts.databinding.ActivityFinalBinding
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.AppUtils.Companion.CafebarToast
import com.kpstv.yts.extensions.utils.CustomMovieLayout
import com.kpstv.yts.extensions.utils.GlideApp
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.interfaces.listener.FavouriteListener
import com.kpstv.yts.interfaces.listener.MovieListener
import com.kpstv.yts.interfaces.listener.SuggestionListener
import com.kpstv.yts.ui.fragments.sheets.BottomSheetDownload
import com.kpstv.yts.ui.fragments.sheets.BottomSheetSubtitles
import com.kpstv.yts.ui.viewmodels.FinalViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty


@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class FinalActivity : AppCompatActivity(), MovieListener {

    companion object {
        const val YOUTUBE_PLAYER_VIEW_REQUEST_CODE = 189
    }

    val TAG = "FinalActivity"

    private val viewModel by viewModels<FinalViewModel>()
    private val binding by viewBinding(ActivityFinalBinding::inflate)

    private lateinit var movie: Movie
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var player: YouTubePlayer
    private var menu: Menu? = null
    private var youTubePlayerCurrentPosition: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppThemeNoAction(this)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
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
        binding.swipeRefreshLayout.isEnabled = false
        binding.swipeRefreshLayout.isRefreshing = true
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
        binding.swipeRefreshLayout.isRefreshing = false;
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
                                Toasty.info(this@FinalActivity, getString(R.string.add_watchlist))
                                    .show()
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
        binding.activityFinalContent.afTitle.text = movie.title
        binding.activityFinalContent.afSubtitle.text =
            "${movie.language} ${AppUtils.getBulletSymbol()} ${movie.year}"
        binding.activityFinalContent.afImdbButton.text = "imdb ${movie.rating}"
        binding.activityFinalContent.afImdbButton.setOnClickListener {
            AppUtils.launchUrl(this@FinalActivity, AppUtils.getImdbUrl(movie.imdb_code))
        }
        binding.activityFinalContent.afPgButton.text =
            if (movie.mpa_rating.isEmpty()) "R" else movie.mpa_rating

        setSummary()

        /** Set recyclerview data */
        binding.activityFinalContent.recyclerViewGenre.layoutManager =
            LinearLayoutManager(this@FinalActivity, LinearLayoutManager.HORIZONTAL, false)
        genreAdapter = GenreAdapter(movie.genres)
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
        binding.activityFinalContent.recyclerViewGenre.adapter = genreAdapter

        binding.activityFinalContent.afcCard.visibility = View.VISIBLE

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
                recommendLayout.injectViewAt(binding.afAddLayout)
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
                similarLayout.injectViewAt(binding.afAddLayout)
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
                    binding.activityFinalPreviews.afYtPreviewImage.setImageBitmap(resource)
                    Log.e(TAG, "Image Loaded")
                    binding.activityFinalPreviews.afYtPreviewImage.setOnClickListener {
                        Log.e(TAG, "OnClicked")
                        if (::player.isInitialized) {
                            player.loadVideo(movie.yt_trailer_id, 0f)
                            binding.activityFinalPreviews.youtubePlayerView.visibility =
                                View.VISIBLE
                        } else {
                            binding.activityFinalPreviews.afYtPreviewPlay.visibility = View.GONE
                            binding.activityFinalPreviews.afYtPreviewProgressBar.visibility =
                                View.VISIBLE
                        }
                    }

                    binding.activityFinalPreviews.afYtPreview.visibility = View.VISIBLE
                }
            })

        GlideApp.with(applicationContext).asBitmap().load(movie.medium_cover_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.activityFinalPreviews.shimmerFrame.hide()

                    binding.activityFinalPreviews.afYtBannerImage.setImageBitmap(resource)
                    binding.activityFinalPreviews.afYtBannerImage.visibility = View.VISIBLE
                    binding.activityFinalPreviews.afYtBannerImage.setOnClickListener {
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
        binding.activityFinalPreviews.root.visibility = View.VISIBLE
    }

    private fun setSummary() {
        val color = AppUtils.getColorFromAttr(this@FinalActivity, R.attr.colorText)

        binding.activityFinalContent.afSummary.text = "${movie.description_full}\n\n"

        /** YTS sometimes doesn't add cast to its movie so we are checking it */

        if (movie.cast?.isNotEmpty() == true) {
            val builder = StringBuilder()
            for (i in 0 until movie.cast?.size!! - 1) {
                builder.append(movie.cast?.get(i)?.name).append("  &#8226;  ")
            }
            builder.append(movie.cast?.get(movie.cast?.size!! - 1)?.name)

            /** If cast not empty add details it to summary */
            binding.activityFinalContent.afSummary.append(
                AppUtils.getColoredString(
                    "<b>Starring</b>",
                    color
                )
            )
            binding.activityFinalContent.afSummary.append(" ${AppUtils.getHtmlText(builder.toString())}\n\n")
        }

        binding.activityFinalContent.afSummary.append(
            AppUtils.getColoredString(
                "<b>Runtime</b>",
                color
            )
        )
        binding.activityFinalContent.afSummary.append(" ${movie.runtime} mins")


        binding.activityFinalContent.afMoreTxt.setOnClickListener {
            if (binding.activityFinalContent.afMoreTxt.text.toString() == "More") {
                binding.activityFinalContent.afMoreTxt.text = "Less"
                binding.activityFinalContent.afSummary.maxLines = Integer.MAX_VALUE;
            } else {
                binding.activityFinalContent.afMoreTxt.text = "More"
                binding.activityFinalContent.afSummary.maxLines = 3;
            }
        }
    }

    private fun setContentButtons() {
        binding.activityFinalContent.afDownload.setOnClickListener {
            loadBottomSheetDownload("download")
        }
        binding.activityFinalContent.afWatch.setOnClickListener {
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
        binding.activityFinalPreviews.youtubePlayerView.enableAutomaticInitialization = false;
        binding.activityFinalPreviews.youtubePlayerView.initialize(object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {

            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                youTubePlayerCurrentPosition = second
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
                binding.activityFinalPreviews.afYtPreviewProgressBar.visibility = View.GONE
                binding.activityFinalPreviews.afYtPreviewPlay.visibility = View.VISIBLE

                binding.activityFinalPreviews.buttonFullscreen.setOnClickListener {
                    player.pause()
                    val i = Intent(this@FinalActivity, PlayerActivity::class.java)
                    i.putExtra("videoId", movie.yt_trailer_id)
                    i.putExtra("lastPlayed", youTubePlayerCurrentPosition)
                    startActivityForResult(i, YOUTUBE_PLAYER_VIEW_REQUEST_CODE)
                }
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> binding.activityFinalPreviews.buttonFullscreen.visibility =
                        View.VISIBLE
                    PlayerConstants.PlayerState.PAUSED -> binding.activityFinalPreviews.buttonFullscreen.visibility =
                        View.GONE
                    PlayerConstants.PlayerState.ENDED -> {
                        binding.activityFinalPreviews.afYtPreviewPlay.visibility = View.VISIBLE
                        binding.activityFinalPreviews.afYtPreviewProgressBar.visibility = View.GONE
                        binding.activityFinalPreviews.youtubePlayerView.visibility = View.GONE
                        binding.activityFinalPreviews.buttonFullscreen.visibility = View.GONE
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
        binding.activityFinalContent.afcCard.visibility = View.GONE
        binding.activityFinalPreviews.root.visibility = View.GONE
        binding.activityFinalPreviews.afYtPreview.visibility = View.GONE
        binding.activityFinalPreviews.afYtBannerImage.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == YOUTUBE_PLAYER_VIEW_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            player.seekTo(data?.getFloatExtra("lastPlayed", 0f) ?: 0f)
            player.play()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {

        if (::player.isInitialized) player.pause()
        binding.activityFinalPreviews.youtubePlayerView.release()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

