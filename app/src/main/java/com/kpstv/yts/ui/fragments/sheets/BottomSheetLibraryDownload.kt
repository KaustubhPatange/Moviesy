package com.kpstv.yts.ui.fragments.sheets

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.adapters.SelectSubAdapter
import com.kpstv.yts.data.models.SelectSubtitle
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.interfaces.listener.SingleClickListener
import com.kpstv.yts.ui.activities.TorrentPlayerActivity
import com.kpstv.yts.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.bottom_sheet_download.view.addLayout
import kotlinx.android.synthetic.main.bottom_sheet_library_download.view.*
import kotlinx.android.synthetic.main.custom_small_tip.view.*

enum class PlaybackType {
    LOCAL,
    REMOTE
}

class BottomSheetLibraryDownload(
    private val viewModel: MainViewModel,
    private val playbackType: PlaybackType
) : BottomSheetDialogFragment() {
    private lateinit var singleAdapter: SelectSubAdapter
    private lateinit var model: Model.response_download

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


                when (playbackType) {
                    PlaybackType.LOCAL -> {

                        val buttonLayout = layoutInflater.inflate(
                            R.layout.button_local_play,
                            view.bottom_sheet_layout
                        )
                        buttonLayout.setOnClickListener {
                            val i = Intent(context, TorrentPlayerActivity::class.java)
                            i.putExtra("normalLink", model.videoPath)
                            i.putExtra("hash", model.hash)

                            if (view.checkBox_playFrom.isVisible) {
                                i.putExtra("lastPosition", model.lastSavedPosition)
                            }

                            if (::singleAdapter.isInitialized) {
                                singleAdapter.models.forEach {
                                    if (it.isChecked) {
                                        i.putExtra("sub", it.text)
                                    }
                                }
                            }

                            startActivity(i)
                            dismiss()
                        }

                     //   view.bottom_sheet_layout.addView(buttonLayout)
                        showSubtitle(view)
                    }
                    PlaybackType.REMOTE -> {
                        val buttonLayout = layoutInflater.inflate(
                            R.layout.button_cast_play,
                            view.bottom_sheet_layout
                        )

                        buttonLayout.setOnClickListener {
                            // TODO: Set onClick listener for button
                        }

                      //  view.bottom_sheet_layout.addView(buttonLayout)
                    }
                }
            }
    }

    private fun showSubtitle(view: View) {
        if (AppInterface.SUBTITLE_LOCATION.listFiles()?.isNotEmpty() == true) {

            /** Filter subtitle to check whether subtitle for current movie exist */

            val titleSpan = model.title.split(" ")[0]

            val onlySuchFiles =
                AppInterface.SUBTITLE_LOCATION.list()
                    ?.filter { f -> f?.contains(titleSpan) == true }
            if (onlySuchFiles?.isNotEmpty() == true) {
                val cssView = LayoutInflater.from(context)
                    .inflate(R.layout.custom_select_subtitle, view.addLayout)
                val recyclerView = cssView.findViewById<RecyclerView>(R.id.recyclerView)

                val list = ArrayList<SelectSubtitle>()
                onlySuchFiles.mapTo(list) { SelectSubtitle(it) }

                singleAdapter = SelectSubAdapter(requireContext(), list)
                singleAdapter.setOnClickListener(object :
                    SingleClickListener {
                    override fun onClick(obj: Any, i: Int) {
                        onlySuchFiles.indices.forEach { c ->
                            if (list[c].isChecked && i != c) {
                                list[c].isChecked = false
                                singleAdapter.notifyItemChanged(c)
                            }
                        }

                        list[i].isChecked = !list[i].isChecked
                        singleAdapter.notifyItemChanged(i)
                    }
                })

                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = singleAdapter
            } else commonNoSubtitle(view)
        } else commonNoSubtitle(view)
    }

    private fun commonNoSubtitle(view: View) {
        val subView =
            LayoutInflater.from(context).inflate(R.layout.custom_small_tip, view.addLayout)
        subView.tip_text.text = getString(R.string.noSubs)
        subView.tip_button.text = getString(R.string.download)
        subView.tip_button.setOnClickListener {
            val bottomSheetSubtitles =
                BottomSheetSubtitles()
            bottomSheetSubtitles.show(activity?.supportFragmentManager!!, model.imdbCode)
            dismiss()
        }
    }
}