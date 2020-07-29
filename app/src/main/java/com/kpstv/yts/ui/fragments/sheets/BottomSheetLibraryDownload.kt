package com.kpstv.yts.ui.fragments.sheets

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.yts.R
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.toFile
import com.kpstv.yts.ui.activities.TorrentPlayerActivity
import com.kpstv.yts.ui.helpers.SubtitleHelper
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.bottom_sheet_download.view.addLayout
import kotlinx.android.synthetic.main.bottom_sheet_library_download.view.*
import kotlinx.android.synthetic.main.button_cast_play.view.*
import kotlinx.android.synthetic.main.button_local_play.view.*

enum class PlaybackType {
    LOCAL,
    REMOTE
}

class BottomSheetLibraryDownload(
    private val castHelper: CastHelper, // TODO: Remove this unused parameter
    private val playbackType: PlaybackType
) : BottomSheetDialogFragment() {
    private lateinit var model: Model.response_download

    private lateinit var subtitleHelper: SubtitleHelper

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_library_download, container, false)
            ?.also { view ->
                /** Get the model from the arguments */
                model = arguments?.getSerializable("model")!! as Model.response_download

                /** Show the last played position */
                if (model.lastSavedPosition != 0) {
                    view.checkBox_playFrom.text =
                        "Play from last save position (${model.lastSavedPosition / (1000 * 60)} mins)"
                } else view.checkBox_playFrom.hide()

                /** Set view according to playback type */
                when (playbackType) {
                    PlaybackType.LOCAL -> { // Local mode
                        layoutInflater.inflate(
                            R.layout.button_local_play,
                            view.bottom_sheet_layout
                        ).apply {
                            this.playButton.setOnClickListener { localPlayButtonClicked(view) }
                        }

                        /** Show subtitle view */
                        showSubtitle(view)
                    }
                    PlaybackType.REMOTE -> { // Cast mode
                        layoutInflater.inflate(
                            R.layout.button_cast_play,
                            view.bottom_sheet_layout
                        ).apply {
                            this.castButton.setOnClickListener { remotePlayButtonClicked(view) }
                        }

                        showSubtitle(view)

                        /** TODO: Show subtitle only if the premium is unlocked *//*
                        if (AppInterface.IS_PREMIUM_UNLOCKED) {
                            showSubtitle(view)
                        } else {
                            PremiumHelper.insertSubtitlePremiumTip(
                                requireActivity(), view.addLayout
                            )
                        }*/
                    }
                }
            }
    }

    private fun remotePlayButtonClicked(view: View) {
        /** Find a subtitle track if exist */
        val subtitleFile = if (::subtitleHelper.isInitialized)
            subtitleHelper.getSelectedSubtitle() else null

        if (model.videoPath != null) {
            /** This must be called before clearing bottom sheet */
            val playFromLastPosition =
                view.checkBox_playFrom.isVisible && view.checkBox_playFrom.isChecked

            /** Clear the bottom sheet view */
            view.bottom_sheet_layout.removeAllViews()
            layoutInflater.inflate(
                R.layout.custom_progress,
                view.bottom_sheet_layout
            )

            castHelper.loadMedia(
                downloadModel = model,
                playFromLastPosition = playFromLastPosition,
                srtFile = subtitleFile,
                onLoadComplete = { error ->
                    if (error != null) {
                        Toasty.error(
                            requireContext(),
                            error.message ?: requireContext().getString(
                                R.string.error_unknown
                            )
                        ).show()
                    }
                    dismiss()
                }
            )
        } else
            Toasty.error(
                requireContext(),
                requireContext().getString(R.string.error_video_path)
            ).show()
    }

    private fun localPlayButtonClicked(view: View) {
        val i = Intent(context, TorrentPlayerActivity::class.java)
        i.putExtra("normalLink", model.videoPath)
        i.putExtra("hash", model.hash)

        if (view.checkBox_playFrom.isVisible && view.checkBox_playFrom.isChecked) {
            i.putExtra("lastPosition", model.lastSavedPosition)
        }

        if (::subtitleHelper.isInitialized) {
            i.putExtra("sub", subtitleHelper.getSelectedSubtitle()?.name)
        }

        startActivity(i)
        dismiss()
    }

    private fun showSubtitle(view: View) {
        subtitleHelper = SubtitleHelper.Builder(requireActivity())
            .setTitle(model.title)
            .setImdbCode(model.imdbCode!!)
            .setParentView(view)
            .setAddLayout(view.addLayout)
            .setParentBottomSheet(this)
            .build().apply {
                populateSubtitleView()
            }
    }
}