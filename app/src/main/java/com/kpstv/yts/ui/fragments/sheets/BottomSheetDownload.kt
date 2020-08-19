package com.kpstv.yts.ui.fragments.sheets

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.adapters.DownloadAdapter
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.databinding.BottomSheetDownloadBinding
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getMagnetUrl
import com.kpstv.yts.extensions.views.ExtendedBottomSheetDialogFragment
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.ui.activities.TorrentPlayerActivity
import com.kpstv.yts.ui.helpers.InterstitialAdHelper
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.helpers.SubtitleHelper
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject

/**
 * Serves two purpose...
 * 1. Handles when "Download" button is clicked
 * 2. Handles when "Watch" button is clicked
 */
@AndroidEntryPoint
class BottomSheetDownload : ExtendedBottomSheetDialogFragment(R.layout.bottom_sheet_download) {

    enum class ViewType {
        DOWNLOAD,
        WATCH
    }

    companion object {
        const val TORRENTS = "com.kpstv.yts.torrent_models"
        const val TITLE = "com.kpstv.yts.title"
        const val IMAGE_URI = "com.kpstv.yts.image_url"
        const val IMDB_CODE = "com.kpstv.yts.imdb_code"
        const val MOVIE_ID = "com.kpstv.yts.movie_id"
    }

    private val binding by viewBinding(BottomSheetDownloadBinding::bind)

    @Inject
    lateinit var interstitialAdHelper: InterstitialAdHelper

    val TAG = "BottomSheetDownload"
    private var bluray = ArrayList<Torrent>()
    private var webrip = ArrayList<Torrent>()
    private lateinit var adapter: DownloadAdapter
    private lateinit var subtitleHelper: SubtitleHelper
    private lateinit var title: String
    private lateinit var imdbCode: String
    private lateinit var imageUri: String
    private lateinit var movieId: Integer

    private lateinit var viewType: ViewType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = arguments?.getSerializable(TORRENTS) as ArrayList<Torrent>
        title = arguments?.getString(TITLE) as String
        imageUri = arguments?.getString(IMAGE_URI) as String
        imdbCode = arguments?.getString(IMDB_CODE) as String
        movieId = arguments?.getInt(MOVIE_ID) as Integer
        binding.recyclerViewDownload.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        viewType = ViewType.valueOf(tag ?: ViewType.DOWNLOAD.name)

        for (t in list) {
            if (t.type == "bluray") bluray.add(t)
            if (t.type == "web") webrip.add(t)
        }

        if (bluray.size <= 0) {
            binding.chipBlueray.visibility = View.GONE
            binding.chipWebrip.isChecked = true
            binding.chipWebrip.isClickable = false
        }
        if (webrip.size <= 0) {
            binding.chipWebrip.visibility = View.GONE
            binding.chipBlueray.isChecked = true
            binding.chipBlueray.isClickable = false
        }

        if (webrip.size > 0 && bluray.size > 0)
            binding.chipBlueray.isChecked = true

        binding.chipBlueray.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) binding.chipWebrip.isChecked = false
            filterChips()
        }

        binding.chipWebrip.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) binding.chipBlueray.isChecked = false
            filterChips()
        }

        filterChips()

        /** Special code for watch now button & Library Button */
        setUpForWatch()
    }

    private fun filterChips() {
        if (binding.chipBlueray.isChecked) {
            adapter = DownloadAdapter(context, bluray)
        } else if (binding.chipWebrip.isCheckable) {
            adapter = DownloadAdapter(context, webrip)
        }

        adapter.setDownloadClickListener(object : DownloadAdapter.DownloadClickListener {
            override fun onClick(torrent: Torrent, pos: Int) {
                /** @Admob Show ads and then do something on complete */
                interstitialAdHelper.showAd {
                    when (viewType) {
                        ViewType.WATCH -> { // When watch button is clicked
                            val i = Intent(
                                requireContext(),
                                TorrentPlayerActivity::class.java
                            )
                            i.putExtra(TorrentPlayerActivity.ARG_TORRENT_LINK, torrent.url)

                            if (::subtitleHelper.isInitialized) {
                                i.putExtra(
                                    TorrentPlayerActivity.ARG_SUBTITLE_NAME,
                                    subtitleHelper.getSelectedSubtitle()?.name
                                )
                            }

                            requireContext().startActivity(i)
                        }
                        ViewType.DOWNLOAD -> { // When download button is clicked
                            if (startService(torrent))
                                Toasty.info(
                                    requireContext(),
                                    getString(R.string.download_started)
                                ).show()
                        }
                    }
                    dismiss()
                }
            }
        })

        adapter.setDownloadLongClickListener(object : DownloadAdapter.DownloadLongClickListener {
            override fun onLongClick(torrent: Torrent, pos: Int) {
                if (viewType == ViewType.DOWNLOAD) {
                    /** @Admob Show ads and then do something on complete */
                    interstitialAdHelper.showAd {
                        val intent = Intent(ACTION_VIEW)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.data = Uri.parse(
                            getMagnetUrl(
                                torrent.url.substring(torrent.url.lastIndexOf("/") + 1), title
                            )
                        )
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toasty.error(requireContext(), getString(R.string.no_action)).show()
                        }
                    }
                }
            }
        })

        binding.recyclerViewDownload.adapter = adapter
    }

    fun startService(model: Torrent): Boolean {
        if (!AppUtils.checkIfServiceIsRunning(
                requireContext(),
                DownloadService::class
            ) && !AppInterface.IS_PREMIUM_UNLOCKED
        ) {
            model.title = title
            model.banner_url = imageUri
            model.imdbCode = imdbCode
            model.movieId = movieId as Int
            val serviceIntent = Intent(requireContext(), DownloadService::class.java)
            serviceIntent.putExtra(DownloadService.TORRENT_JOB, model)
            requireContext().startService(serviceIntent)

            return true
        } else {
            PremiumHelper.showDownloadPremium(requireActivity())
            return false
        }
    }

    private fun setUpForWatch() {
        if (viewType == ViewType.WATCH) {
            binding.itemTipText.visibility = View.GONE

            /** Show subtitles */
            showSubtitle()
        }
    }

    private fun showSubtitle() {
        subtitleHelper = SubtitleHelper.Builder(requireActivity())
            .setTitle(title)
            .setImdbCode(imdbCode)
            .setParentView(binding.root)
            .setAddLayout(binding.addLayout)
            .setParentBottomSheet(this)
            .build()
            .apply {
                populateSubtitleView()
            }
    }
}
