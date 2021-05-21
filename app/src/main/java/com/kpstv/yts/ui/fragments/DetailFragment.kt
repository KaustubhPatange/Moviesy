package com.kpstv.yts.ui.fragments

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.common_moviesy.extensions.utils.CommonUtils
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.ValueFragment
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.adapters.GenreAdapter
import com.kpstv.yts.data.converters.GenreEnumConverter
import com.kpstv.yts.data.models.Cast
import com.kpstv.yts.data.models.Crew
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.databinding.FragmentDetailBinding
import com.kpstv.yts.extensions.*
import com.kpstv.yts.extensions.common.CustomMovieLayout
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.GlideApp
import com.kpstv.yts.extensions.utils.LangCodeUtils
import com.kpstv.yts.extensions.views.ExtendedNestedScrollView
import com.kpstv.yts.interfaces.listener.MovieListener
import com.kpstv.yts.ui.activities.ImageViewActivity
import com.kpstv.yts.ui.activities.PlayerActivity
import com.kpstv.yts.ui.fragments.sheets.BottomSheetDownload
import com.kpstv.yts.ui.fragments.sheets.BottomSheetSubtitles
import com.kpstv.yts.ui.viewmodels.FinalViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.parcel.Parcelize

@AndroidEntryPoint
class DetailFragment : ValueFragment(R.layout.fragment_detail), MovieListener {
    companion object {
        const val YOUTUBE_PLAYER_VIEW_REQUEST_CODE = 189
        private const val SCROLL_STATE = "com.kpstv.yts:detailfragment:scroll_state"
    }

    private val binding by viewBinding(FragmentDetailBinding::bind)
    private val viewModel by viewModels<FinalViewModel>()
    private val navViewModel by activityViewModels<StartViewModel>()
    private lateinit var movieDetail: Movie
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var player: YouTubePlayer
    private var youTubePlayerCurrentPosition: Float = 0f

    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().transparentNavigationBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()

        /** Initializing YouTube player instance */
        initializeYoutubePlayer()

        val args = getKeyArgs<Args>()

        /** Getting movie details for movieId */
        when {
            args.tmDbId != null -> {
                /** Route TMDB movie id */
                viewModel.getMovieDetailTMdb(this, args.tmDbId)
            }
            args.ytsId != null -> {
                /** Route YTS movie id */
                viewModel.getMovieDetailYTS(this, args.ytsId)
            }
            args.movieUrl != null -> {
                /** Parse YTS movie url (usually from a deep link) */
                viewModel.fetchMovieUrl(this, args.movieUrl)
            }
            else -> {
                Toasty.error(requireContext(), getString(R.string.invalid_movieId)).show()
            }
        }
    }

    override fun onStarted() {
        hideLayout()
        binding.swipeRefreshLayout.isEnabled = false
        binding.swipeRefreshLayout.isRefreshing = true
    }

    override fun onFailure(e: Exception) {
        AppInterface.handleRetrofitError(requireContext(), e) {
            goBack()
        }
        e.printStackTrace()
        Log.e(TAG, "Failed--> " + e.message)
    }

    override fun onCastFetched(casts: List<Cast>, crews: List<Crew>) {
        this.movieDetail.cast = casts
        this.movieDetail.crew = crews
        setSummary()
    }

    override fun onComplete(movie: Movie) {
        this.movieDetail = movie
        binding.root.enableDelayedTransition()
        setMovieMenu()
        loadData()
        setPreviews()
        loadSimilar()
        loadRecommendation()
        loadCastMovies()
        setContentButtons()
        binding.root.doOnPreDraw {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setSummary(): Unit = with(binding.activityFinalContent) {
        val color = CommonUtils.getColorFromAttr(requireActivity(), R.attr.colorText)

        afSummary.text = "${movieDetail.description_full}\n\n"

        /** YTS sometimes doesn't add cast to its movie so we are checking it */

        if (movieDetail.cast?.isNotEmpty() == true) {
            val builder = StringBuilder()
            for (i in 0 until movieDetail.cast?.size!! - 1) {
                builder.append(movieDetail.cast?.get(i)?.name).append("  &#8226;  ")
            }
            builder.append(movieDetail.cast?.get(movieDetail.cast?.size!! - 1)?.name)

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

        val crews = movieDetail.crew
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
        afSummary.append(" ${movieDetail.runtime} mins")

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

    /** This is used to set menu i.e to show favourite icon properly
     *  to the menu item based on if Favourite exist or not.
     */
    private fun setMovieMenu() {
        viewModel.isMovieFavourite(movieDetail.id).observe(this, Observer { value ->
            if (value) {
                binding.toolbar.menu?.getItem(0)?.icon =
                    drawableFrom(R.drawable.ic_favorite_yes).apply {
                        this?.setTint(colorFrom(R.color.colorAccent_Dark))
                    }
            }
            else {
                binding.toolbar.menu?.getItem(0)?.icon =
                    drawableFrom(R.drawable.ic_favorite_no)
            }
        })
    }

    private fun loadData() {
        /** Set top data */
        binding.activityFinalContent.afTitle.text = movieDetail.title
        binding.activityFinalContent.afSubtitle.text =
            "${LangCodeUtils.parse(movieDetail.language)} ${AppUtils.getBulletSymbol()} ${movieDetail.year}"
        binding.activityFinalContent.afImdbButton.text = "imdb ${movieDetail.rating}"
        binding.activityFinalContent.afImdbButton.setOnClickListener {
            AppUtils.launchUrl(requireContext(), AppUtils.getImdbUrl(movieDetail.imdb_code))
        }
        binding.activityFinalContent.afPgButton.text =
            if (movieDetail.mpa_rating.isEmpty()) "R" else movieDetail.mpa_rating

        setSummary()

        /** Set recyclerview data */
        binding.activityFinalContent.recyclerViewGenre.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        genreAdapter = GenreAdapter(movieDetail.genres)
        genreAdapter.setOnClickListener(object : GenreAdapter.OnClickListener {
            override fun onClick(text: String, position: Int) {
                val genre = GenreEnumConverter.toGenrefromString(text)
                if (genre != null) {
                    val queryMap = YTSQuery.ListMoviesBuilder().apply {
                        setGenre(genre)
                        setOrderBy(YTSQuery.OrderBy.descending)
                    }.build()
                    navViewModel.goToMore("${getString(R.string.genre_select)} $text", queryMap, add = true)
                } else Toasty.error(requireContext(), "Genre $text does not exist").show()
            }
        })
        binding.activityFinalContent.recyclerViewGenre.adapter = genreAdapter

        binding.activityFinalContent.root.visibility = View.VISIBLE
    }

    private fun setPreviews() {
        GlideApp.with(requireView()).asBitmap().load(movieDetail.background_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.root.enableDelayedTransition()
                    binding.activityFinalPreviews.afYtPreviewImage.setImageBitmap(resource)
                    Log.e(TAG, "Image Loaded")
                    binding.activityFinalPreviews.afYtPreviewImage.setOnClickListener {
                        Log.e(TAG, "OnClicked")
                        if (::player.isInitialized) {
                            player.loadVideo(movieDetail.yt_trailer_id, 0f)
                            binding.activityFinalPreviews.youtubePlayerView.show()
                        } else {
                            binding.activityFinalPreviews.afYtPreviewPlay.hide()
                            binding.activityFinalPreviews.afYtPreviewProgressBar.show()
                        }
                    }

                    binding.activityFinalPreviews.afYtPreview.show()
                }
            })

        GlideApp.with(requireView()).asBitmap().load(movieDetail.medium_cover_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.activityFinalPreviews.shimmerFrame.hide()

                    binding.activityFinalPreviews.afYtBannerImage.setImageBitmap(resource)
                    binding.activityFinalPreviews.afYtBannerImage.visibility = View.VISIBLE
                    binding.activityFinalPreviews.afYtBannerImage.setOnClickListener {
                        val intent = Intent(requireContext(), ImageViewActivity::class.java) // TODO: Migrate to ImageFragment
                        intent.putExtra(ImageViewActivity.IMAGE_URL, movieDetail.large_cover_image)
                        val options = ActivityOptions
                            .makeSceneTransitionAnimation(
                                requireActivity(),
                                binding.activityFinalPreviews.afYtBannerImage,
                                "banner_photo"
                            )
                        startActivity(intent, options.toBundle())
                    }
                }
            })
        binding.activityFinalPreviews.root.visibility = View.VISIBLE
    }

    private fun loadRecommendation() {
        val suggestionListener = SuggestionCallback(
            onComplete = { movies, tag, isMoreAvailable ->
                val recommendLayout = CustomMovieLayout(requireContext(), getString(R.string.recommend))
                recommendLayout.injectViewAt(binding.afSuggestionAddLayout)
                recommendLayout.listenForClicks { view, movie ->
                    view.scaleInOut()
                    navViewModel.goToDetail(tmDbId = movie.movieId?.toString(), add = true)
                }
                recommendLayout.setupCallbacks(navViewModel, movies, "$tag/recommendations", isMoreAvailable)
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )

        viewModel.getRecommendations(movieDetail.imdb_code, suggestionListener)
    }

    private fun loadSimilar() {
        val suggestionListener = SuggestionCallback(
            onComplete = { movies, tag, isMoreAvailable ->
                val similarLayout = CustomMovieLayout(requireContext(), getString(R.string.suggested))
                similarLayout.injectViewAt(binding.afSuggestionAddLayout)
                similarLayout.listenForClicks { view, movie ->
                    view.scaleInOut()
                    navViewModel.goToDetail(tmDbId = movie.movieId?.toString(), add = true)
                }
                similarLayout.setupCallbacks(navViewModel, movies, "${movieDetail.imdb_code}/similar", isMoreAvailable)
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )
        viewModel.getSuggestions(movieDetail.imdb_code, suggestionListener)
    }

    private fun loadCastMovies() {
        val castCallback = CastMoviesCallback(
            onComplete = { results ->
                results.forEach {
                    val movieLayout = CustomMovieLayout(requireContext(), "${getString(R.string.more_with)} ${it.name}")
                    movieLayout.injectViewAt(binding.afMoreAddLayout)
                    movieLayout.listenForClicks { view, movie ->
                        view.scaleInOut()
                        navViewModel.goToDetail(tmDbId = movie.movieId?.toString(), add = true)
                    }
                    movieLayout.setupCallbacks(navViewModel, movieDetail.title, it.movies)
                }
            },
            onFailure = { e ->
                Log.e(TAG, "Failed: ${e.message}", e)
            }
        )
        viewModel.getTopCrewMovies(movieDetail.imdb_code, castCallback)
    }

    private fun setContentButtons() {
        binding.activityFinalContent.afDownload.setOnClickListener {
            loadBottomSheetDownload(BottomSheetDownload.ViewType.DOWNLOAD)
        }
        binding.activityFinalContent.afWatch.setOnClickListener {
            loadBottomSheetDownload(BottomSheetDownload.ViewType.WATCH)
        }
    }

    private fun loadBottomSheetDownload(type: BottomSheetDownload.ViewType) {
        Permissions.verifyStoragePermission(this) {
            val args = BottomSheetDownload.Args(
                type = type.name,
                torrents = movieDetail.torrents,
                title = movieDetail.title,
                imdbCode = movieDetail.imdb_code,
                mediumImageCover = movieDetail.medium_cover_image,
                movieId = movieDetail.id
            )
            getSimpleNavigator().show(BottomSheetDownload::class, args)
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

    private fun setToolbar() {
        binding.appbarLayout.applyTopInsets()
        binding.afMoreAddLayout.applyBottomInsets(extra = 30.dp().toInt())
        binding.toolbar.title = " "
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { goBack() }
        binding.toolbar.inflateMenu(R.menu.activity_final_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (::movieDetail.isInitialized) {
                when (item.itemId) {
                    R.id.action_subtitles -> {
                        Permissions.verifyStoragePermission(this) {
                            val sheet = BottomSheetSubtitles()
                            sheet.show(childFragmentManager, movieDetail.imdb_code)
                        }
                    }
                    R.id.action_favourite -> {
                        viewModel.toggleFavourite(movieDetail) {
                            Toasty.info(requireContext(), getString(R.string.add_watchlist)).show()
                        }
                    }
                    R.id.action_share -> {
                        AppUtils.shareUrl(requireActivity(), movieDetail.url)
                    }
                }
            } else
                Toasty.error(requireContext(), getString(R.string.movie_not_loaded)).show()
            return@setOnMenuItemClickListener true
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
                        val i = Intent(requireContext(), PlayerActivity::class.java)
                        i.putExtra(PlayerActivity.VIDEO_ID, movieDetail.yt_trailer_id)
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

    override fun onViewStateChanged(viewState: ViewState) {
        super.onViewStateChanged(viewState)
        if (viewState == ViewState.BACKGROUND) {
            if (::player.isInitialized) player.pause()
        }
    }

    override fun onStop() {
        // TODO: Find a way to somehow restore it.
        viewModel.detailState.nestedScrollState = binding.nestedScrollView.onSaveInstanceState()
        super.onStop()
    }

    override fun onDestroyView() {
        if (::player.isInitialized) player.pause()
        requireView().findViewById<YouTubePlayerView>(R.id.youtube_player_view).release()
        super.onDestroyView()
    }

    @Parcelize
    data class Args(
        val tmDbId: String? = null,
        val ytsId: Int? = null,
        val movieUrl: String? = null
    ) : BaseArgs(), Parcelable
}