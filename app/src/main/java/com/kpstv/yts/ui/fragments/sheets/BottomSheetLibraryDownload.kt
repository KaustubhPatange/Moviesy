package com.kpstv.yts.ui.fragments.sheets

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.cast.CastHelper
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.databinding.BottomSheetLibraryDownloadBinding
import com.kpstv.yts.databinding.ButtonCastPlayBinding
import com.kpstv.yts.databinding.ButtonLocalPlayBinding
import com.kpstv.yts.databinding.CustomProgressBinding
import com.kpstv.yts.extensions.ExtendedBottomSheetDialogFragment
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.viewBinding
import com.kpstv.yts.ui.activities.TorrentPlayerActivity
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.helpers.SubtitleHelper
import es.dmoral.toasty.Toasty

enum class PlaybackType {
    LOCAL,
    REMOTE
}

class BottomSheetLibraryDownload(
    private val castHelper: CastHelper, // TODO: Remove this unused parameter
    private val playbackType: PlaybackType
) : ExtendedBottomSheetDialogFragment(R.layout.bottom_sheet_library_download) {

    private val binding by viewBinding(BottomSheetLibraryDownloadBinding::bind)

    private lateinit var model: Model.response_download
    private lateinit var subtitleHelper: SubtitleHelper

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Get the model from the arguments */
        model = arguments?.getSerializable("model")!! as Model.response_download

        /** Show the last played position */
        if (model.lastSavedPosition != 0) {
            binding.checkBoxPlayFrom.text =
                "Play from last save position (${model.lastSavedPosition / (1000 * 60)} mins)"
        } else binding.checkBoxPlayFrom.hide()

        /** Set view according to playback type */
        when (playbackType) {
            PlaybackType.LOCAL -> { // Local mode

                ButtonLocalPlayBinding.inflate(layoutInflater, binding.root, true).apply {
                    playButton.setOnClickListener { localPlayButtonClicked() }
                }

                /** Show subtitle view */
                showSubtitle()
            }
            PlaybackType.REMOTE -> { // Cast mode

                ButtonCastPlayBinding.inflate(layoutInflater, binding.root, true).apply {
                    castButton.setOnClickListener { remotePlayButtonClicked() }
                }

                /** Show subtitle only if the premium is unlocked */
                if (AppInterface.IS_PREMIUM_UNLOCKED) {
                    showSubtitle()
                } else {
                    PremiumHelper.insertSubtitlePremiumTip(
                        requireActivity(), binding.addLayout
                    )
                }
            }
        }
    }

    private fun remotePlayButtonClicked() {
        /** Find a subtitle track if exist */
        val subtitleFile = if (::subtitleHelper.isInitialized)
            subtitleHelper.getSelectedSubtitle() else null

        if (model.videoPath != null) {
            /** This must be called before clearing bottom sheet */
            val playFromLastPosition =
                binding.checkBoxPlayFrom.isVisible && binding.checkBoxPlayFrom.isChecked

            /** Clear the bottom sheet view */
            binding.root.removeAllViews()
            CustomProgressBinding.inflate(layoutInflater, binding.root, true)

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

    private fun localPlayButtonClicked() {
        val i = Intent(context, TorrentPlayerActivity::class.java)
        i.putExtra("normalLink", model.videoPath)
        i.putExtra("hash", model.hash)

        if (binding.checkBoxPlayFrom.isVisible && binding.checkBoxPlayFrom.isChecked) {
            i.putExtra("lastPosition", model.lastSavedPosition)
        }

        if (::subtitleHelper.isInitialized) {
            i.putExtra("sub", subtitleHelper.getSelectedSubtitle()?.name)
        }

        startActivity(i)
        dismiss()
    }

    private fun showSubtitle() {
        subtitleHelper = SubtitleHelper.Builder(requireActivity())
            .setTitle(model.title)
            .setImdbCode(model.imdbCode!!)
            .setParentView(binding.root)
            .setAddLayout(binding.addLayout)
            .setParentBottomSheet(this)
            .build().apply {
                populateSubtitleView()
            }
    }
}