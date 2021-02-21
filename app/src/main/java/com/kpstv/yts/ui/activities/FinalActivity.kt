package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.common_moviesy.extensions.utils.CommonUtils
import com.kpstv.yts.AppInterface.Companion.MOVIE_ID
import com.kpstv.yts.AppInterface.Companion.handleRetrofitError
import com.kpstv.yts.AppInterface.Companion.setAppThemeNoAction
import com.kpstv.yts.R
import com.kpstv.yts.adapters.GenreAdapter
import com.kpstv.yts.data.converters.GenreEnumConverter
import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Crew
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.databinding.ActivityFinalBinding
import com.kpstv.yts.extensions.*
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.AppUtils.Companion.CafebarToast
import com.kpstv.yts.extensions.utils.GlideApp
import com.kpstv.yts.extensions.utils.LangCodeUtils
import com.kpstv.yts.interfaces.listener.MovieListener
import com.kpstv.yts.ui.fragments.sheets.BottomSheetDownload
import com.kpstv.yts.ui.fragments.sheets.BottomSheetSubtitles
import com.kpstv.yts.ui.viewmodels.FinalViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty


/**
 * Pass movieId as extras in Intent
 * as String: routes for TMDB movie
 * as Int: routes for YTS movie
 */
@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class FinalActivity : AppCompatActivity(), MovieListener {

    companion object {
        const val YOUTUBE_PLAYER_VIEW_REQUEST_CODE = 189
        const val MOVIE_URL = "com.kpstv.moviesy.movie_url"
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

        /** Initializing YouTube player instance */
        initializeYoutubePlayer()

        val movieId = intent.extras?.get(MOVIE_ID)
        val movieUrl = intent.extras?.getString(MOVIE_URL)

        /** Getting movie details for movieId */
        when {
            movieId is String -> {
                /** Route TMDB movie id */
                viewModel.getMovieDetailTMdb(this, movieId)
            }
            movieId is Int -> {
                /** Route YTS movie id */
                viewModel.getMovieDetailYTS(this, movieId)
            }
            movieUrl != null -> {
                /** Parse YTS movie url (usually from a deep link) */
                viewModel.fetchMovieUrl(this, movieUrl)
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

    override fun onCastFetched(casts: List<Cast>, crews: List<Crew>) {
        this.movie.cast = casts
        this.movie.crew = crews
        setSummary()
    }

    override fun onComplete(movie: Movie) {
        this.movie = movie

        binding.root.enableDelayedTransition()

        setMovieMenu()

        loadData()

        setPreviews()

        loadSimilar()

        loadRecommendation()

        loadCastMovies()

        setContentButtons()
        binding.swipeRefreshLayout.isRefreshing = false
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
                    Permissions.verifyStoragePermission(this) {
                        BottomSheetSubtitles().apply {
                            show(supportFragmentManager, movie.imdb_code)
                        }
                    }
                }
                R.id.action_favourite -> {
                    viewModel.toggleFavourite(movie) {
                        Toasty.info(this@FinalActivity, getString(R.string.add_watchlist)).show()
                    }
                }
                R.id.action_share -> {
                    AppUtils.shareUrl(this, movie.url)
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
        viewModel.isMovieFavourite(movie.id).observe(this, Observer { value ->
            if (value)
                menu?.getItem(0)?.icon =
                    drawableFrom(R.drawable.ic_favorite_yes).apply {
                        this?.setTint(colorFrom(R.color.colorAccent_Dark))
                    }
            else
                menu?.getItem(0)?.icon =
                    drawableFrom(R.drawable.ic_favorite_no)
        })
    }

    private fun loadData() {

        /** Set top data */
        binding.activityFinalContent.afTitle.text = movie.title
        binding.activityFinalContent.afSubtitle.text =
            "${LangCodeUtils.parse(movie.language)} ${AppUtils.getBulletSymbol()} ${movie.year}"
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

        binding.activityFinalContent.root.visibility = View.VISIBLE
    }

    private fun loadRecommendation() {
        val suggestionListener = SuggestionCallback(
            onComplete = { movies, tag, isMoreAvailable ->
                val recommendLayout =
                    CustomMovieLayout(
                        this@FinalActivity,
                        getString(R.string.recommend)
                    )
                recommendLayout.injectViewAt(binding.afSuggestionAddLayout)
                recommendLayout.setupCallbacks(movies, "$tag/recommendations", isMoreAvailable)
            },
            onFailure = { e ->
                handleRetrofitError(this@FinalActivity, e)
                e.printStackTrace()
            }
        )

        viewModel.getRecommendations(movie.imdb_code, suggestionListener)
    }

    private fun loadSimilar() {
        val suggestionListener = SuggestionCallback(
            onComplete = { movies, tag, isMoreAvailable ->
                val similarLayout =
                    CustomMovieLayout(
                        this@FinalActivity,
                        getString(R.string.suggested)
                    )
                similarLayout.injectViewAt(binding.afSuggestionAddLayout)
                similarLayout.setupCallbacks(movies, "${movie.imdb_code}/similar", isMoreAvailable)
            },
            onFailure = { e ->
                handleRetrofitError(this@FinalActivity, e)
                e.printStackTrace()
            }
        )

        viewModel.getSuggestions(movie.imdb_code, suggestionListener)
    }

    private fun loadCastMovies() {
        val castCallback = CastMoviesCallback(
            onComplete = { results ->
                results.forEach {
                    val movieLayout = CustomMovieLayout(
                        this@FinalActivity,
                        "${getString(R.string.more_with)} ${it.name}"
                    )
                    movieLayout.injectViewAt(binding.afMoreAddLayout)
                    movieLayout.setupCallbacks(movie.title, it.movies)
                }
            },
            onFailure = { e ->
                Log.e(TAG, "Failed: ${e.message}", e)
            }
        )

        viewModel.getTopCrewMovies(movie.imdb_code, castCallback)
    }

    private fun setPreviews() {
        GlideApp.with(applicationContext).asBitmap().load(movie.background_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.root.enableDelayedTransition()
                    binding.activityFinalPreviews.afYtPreviewImage.setImageBitmap(resource)
                    Log.e(TAG, "Image Loaded")
                    binding.activityFinalPreviews.afYtPreviewImage.setOnClickListener {
                        Log.e(TAG, "OnClicked")
                        if (::player.isInitialized) {
                            player.loadVideo(movie.yt_trailer_id, 0f)
                            binding.activityFinalPreviews.youtubePlayerView.show()
                        } else {
                            binding.activityFinalPreviews.afYtPreviewPlay.hide()
                            binding.activityFinalPreviews.afYtPreviewProgressBar.show()
                        }
                    }

                    binding.activityFinalPreviews.afYtPreview.show()
                }
            })

        GlideApp.with(applicationContext).asBitmap().load(movie.medium_cover_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.activityFinalPreviews.shimmerFrame.hide()

                    binding.activityFinalPreviews.afYtBannerImage.setImageBitmap(resource)
                    binding.activityFinalPreviews.afYtBannerImage.visibility = View.VISIBLE
                    binding.activityFinalPreviews.afYtBannerImage.setOnClickListener {
                        val intent = Intent(this@FinalActivity, ImageViewActivity::class.java)
                        intent.putExtra(ImageViewActivity.IMAGE_URL, movie.large_cover_image)
                        val options = ActivityOptions
                            .makeSceneTransitionAnimation(
                                this@FinalActivity,
                                binding.activityFinalPreviews.afYtBannerImage,
                                "banner_photo"
                            )
                        startActivity(intent, options.toBundle())
                    }
                }
            })
        binding.activityFinalPreviews.root.visibility = View.VISIBLE
    }

    private fun setSummary(): Unit = with(binding.activityFinalContent) {
        val color = CommonUtils.getColorFromAttr(this@FinalActivity, R.attr.colorText)

        afSummary.text = "${movie.description_full}\n\n"

        /** YTS sometimes doesn't add cast to its movie so we are checking it */

        if (movie.cast?.isNotEmpty() == true) {
            val builder = StringBuilder()
            for (i in 0 until movie.cast?.size!! - 1) {
                builder.append(movie.cast?.get(i)?.name).append("  &#8226;  ")
            }
            builder.append(movie.cast?.get(movie.cast?.size!! - 1)?.name)

            /** If cast not empty add details it to summary */
            afSummary.append(
                CommonUtils.getColoredString(
                    "<b>Starring</b>",
                    color
                )
            )
            afSummary.append(" ${CommonUtils.getHtmlText(builder.toString())}\n\n")
        }

        /** Injecting director info */

        val crews = movie.crew
        if (crews?.isNotEmpty() == true) {
            val directors = crews.joinToString(separator = "  &#8226;  ") { it.name }
            afSummary.append(
                CommonUtils.getColoredString(
                    "<b>Director</b>",
                    color
                )
            )
            afSummary.append(" ${CommonUtils.getHtmlText(directors)}\n\n")
        }

        afSummary.append(
            CommonUtils.getColoredString(
                "<b>Runtime</b>",
                color
            )
        )
        afSummary.append(" ${movie.runtime} mins")

        /** Following code is optimized for handling,
         *  1. Auto resizing the textView when this button is clicked.
         *  2. TextView will only be selected in expanded view.
         *  3. When textView is in selection mode, it will auto cancel it
         *     when this button is clicked.
         */
        afMoreTxt.setOnClickListener {
            binding.root.enableDelayedTransition()
            afSummary.setTextIsSelectable(false)
            if (afMoreTxt.text.toString() == getString(R.string.more)) {
                afSummary.setTextIsSelectable(true)
                afMoreTxt.text = getString(R.string.less)
                afSummary.maxLines = Integer.MAX_VALUE
            } else {
                afMoreTxt.text = getString(R.string.more)
                afSummary.maxLines = 3
            }
            afSummary.moveCursorToVisibleOffset()
        }
    }

    private fun setContentButtons() {
        binding.activityFinalContent.afDownload.setOnClickListener {
            loadBottomSheetDownload(BottomSheetDownload.ViewType.DOWNLOAD)
        }
        binding.activityFinalContent.afWatch.setOnClickListener {
            loadBottomSheetDownload(BottomSheetDownload.ViewType.WATCH)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permissions.processStoragePermission(requestCode, grantResults)
    }

    private fun loadBottomSheetDownload(type: BottomSheetDownload.ViewType) {
        Permissions.verifyStoragePermission(this) {
            val sheet = BottomSheetDownload()
            val bundle = Bundle()
            bundle.putSerializable(BottomSheetDownload.TORRENTS, movie.torrents)
            bundle.putString(BottomSheetDownload.TITLE, movie.title)
            bundle.putString(BottomSheetDownload.IMDB_CODE, movie.imdb_code)
            bundle.putString(BottomSheetDownload.IMAGE_URI, movie.medium_cover_image)
            bundle.putInt(BottomSheetDownload.MOVIE_ID, movie.id)
            sheet.arguments = bundle
            sheet.show(supportFragmentManager, type.name)
        }
    }

    private fun initializeYoutubePlayer() {
        with(binding.activityFinalPreviews) {
            youtubePlayerView.enableAutomaticInitialization = false;
            youtubePlayerView.initialize(object : YouTubePlayerListener by DelegatedYouTubePlayerListener {
                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    youTubePlayerCurrentPosition = second
                }

                override fun onReady(youTubePlayer: YouTubePlayer) {
                    Log.e(TAG, "YouTubePlayerInitialized")
                    player = youTubePlayer
                    afYtPreviewProgressBar.visibility = View.GONE
                    afYtPreviewPlay.visibility = View.VISIBLE

                    buttonFullscreen.setOnClickListener {
                        player.pause()
                        val i = Intent(this@FinalActivity, PlayerActivity::class.java)
                        i.putExtra(PlayerActivity.VIDEO_ID, movie.yt_trailer_id)
                        i.putExtra(PlayerActivity.LAST_PLAYED, youTubePlayerCurrentPosition)
                        startActivityForResult(i, YOUTUBE_PLAYER_VIEW_REQUEST_CODE)
                    }
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    when (state) {
                        PlayerConstants.PlayerState.PLAYING -> buttonFullscreen.show()
                        PlayerConstants.PlayerState.PAUSED -> buttonFullscreen.hide()
                        PlayerConstants.PlayerState.ENDED -> {
                            binding.activityFinalPreviews.afYtPreviewPlay.visibility = View.VISIBLE
                            binding.activityFinalPreviews.afYtPreviewProgressBar.visibility = View.GONE
                            binding.activityFinalPreviews.youtubePlayerView.visibility = View.GONE
                            binding.activityFinalPreviews.buttonFullscreen.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }

    private fun hideLayout() {
        binding.activityFinalContent.root.visibility = View.GONE
        binding.activityFinalPreviews.root.visibility = View.GONE
        binding.activityFinalPreviews.afYtPreview.visibility = View.GONE
        binding.activityFinalPreviews.afYtBannerImage.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == YOUTUBE_PLAYER_VIEW_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (::player.isInitialized) {
                player.seekTo(data?.getFloatExtra(PlayerActivity.LAST_PLAYED, 0f) ?: 0f)
                player.play()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        if (::player.isInitialized) player.pause()
        binding.activityFinalPreviews.youtubePlayerView.release()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (AppUtils.isLastActivity(this))
            startActivity(Intent(this, MainActivity::class.java))
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
            finishAfterTransition() // Fix b\139738913
        else
            super.onBackPressed()
    }
}

