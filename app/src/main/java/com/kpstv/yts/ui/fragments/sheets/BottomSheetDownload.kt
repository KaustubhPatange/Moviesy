package com.kpstv.yts.ui.fragments.sheets

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpstv.yts.R
import com.kpstv.yts.adapters.DownloadAdapter
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.databinding.BottomSheetDownloadBinding
import com.kpstv.yts.extensions.ExtendedBottomSheetDialogFragment
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getMagnetUrl
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.ui.activities.TorrentPlayerActivity
import com.kpstv.yts.ui.helpers.SubtitleHelper

@Suppress("NAME_SHADOWING")
class BottomSheetDownload : ExtendedBottomSheetDialogFragment(R.layout.bottom_sheet_download) {

    private val binding by viewBinding(BottomSheetDownloadBinding::bind)

    val TAG = "BottomSheetDownload"
    private var bluray = ArrayList<Torrent>()
    private var webrip = ArrayList<Torrent>()
    private lateinit var adapter: DownloadAdapter
    private lateinit var subtitleHelper: SubtitleHelper
    private lateinit var title: String
    private lateinit var imdbCode: String
    private lateinit var imageUri: String
    private lateinit var movieId: Integer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = arguments?.getSerializable("models") as ArrayList<Torrent>
        title = arguments?.getString("title") as String
        imageUri = arguments?.getString("imageUri") as String
        imdbCode = arguments?.getString("imdbCode") as String
        movieId = arguments?.getInt("movieId") as Integer
        binding.recyclerViewDownload.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

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

    fun startService(model: Torrent) {
        model.title = title
        model.banner_url = imageUri
        model.imdbCode = imdbCode
        model.movieId = movieId as Int
        val serviceIntent = Intent(context, DownloadService::class.java)
        serviceIntent.putExtra("addJob", model)
        ContextCompat.startForegroundService(context as Context, serviceIntent)
    }

    private fun filterChips() {
        if (binding.chipBlueray.isChecked) {
            adapter = DownloadAdapter(context, bluray)
        } else if (binding.chipWebrip.isCheckable) {
            adapter = DownloadAdapter(context, webrip)
        }

        adapter.setDownloadClickListener(object : DownloadAdapter.DownloadClickListener {
            override fun onClick(torrent: Torrent, pos: Int) {
                when (tag) {
                    "watch_now" -> {
                        val i = Intent(
                            context,
                            TorrentPlayerActivity::class.java
                        )
                        i.putExtra("torrentLink", torrent.url)

                        if (::subtitleHelper.isInitialized) {
                            i.putExtra("sub", subtitleHelper.getSelectedSubtitle()?.name)
                        }

                        context?.startActivity(i)
                    }
                    else -> {
                        startService(torrent)
                        Toast.makeText(
                            context,
                            "Download started, check notification",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dismiss()
            }
        })

        adapter.setDownloadLongClickListener(object : DownloadAdapter.DownloadLongClickListener {
            override fun onLongClick(torrent: Torrent, pos: Int) {
                if (tag != "watch_now") {
                    val intent = Intent(ACTION_VIEW)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.data = Uri.parse(
                        getMagnetUrl(
                            torrent.url.substring(torrent.url.lastIndexOf("/") + 1), title
                        )
                    )
                    context?.startActivity(intent)
                }
            }
        })

        binding.recyclerViewDownload.adapter = adapter
    }

    private fun setUpForWatch() {
        if (tag == "watch_now") {
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
