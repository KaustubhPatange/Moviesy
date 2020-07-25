package com.kpstv.yts.ui.fragments.sheets

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.yts.AppInterface.Companion.SUBTITLE_LOCATION
import com.kpstv.yts.R
import com.kpstv.yts.ui.activities.TorrentPlayerActivity
import com.kpstv.yts.adapters.DownloadAdapter
import com.kpstv.yts.adapters.SelectSubAdapter
import com.kpstv.yts.data.models.SelectSubtitle
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.services.DownloadService
import com.kpstv.yts.interfaces.listener.SingleClickListener
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getMagnetUrl
import kotlinx.android.synthetic.main.bottom_sheet_download.view.*
import kotlinx.android.synthetic.main.custom_small_tip.view.*


@Suppress("NAME_SHADOWING")
class BottomSheetDownload : BottomSheetDialogFragment() {

    val TAG = "BottomSheetDownload"
    private var bluray = ArrayList<Torrent>()
    private var webrip = ArrayList<Torrent>()
    private lateinit var adapter: DownloadAdapter
    private lateinit var singleAdapter: SelectSubAdapter
    private lateinit var title: String
    private lateinit var imdbCode: String
    private lateinit var imageUri: String
    private lateinit var movieId: Integer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_download, container, false) as View

        val list = arguments?.getSerializable("models") as ArrayList<Torrent>
        title = arguments?.getString("title") as String
        imageUri = arguments?.getString("imageUri") as String
        imdbCode = arguments?.getString("imdbCode") as String
        movieId = arguments?.getInt("movieId") as Integer
        view.recyclerView_download.layoutManager =
            LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)

        for (t in list) {
            if (t.type == "bluray") bluray.add(t)
            if (t.type == "web") webrip.add(t)
        }

        if (bluray.size<=0) {
            view.chip_blueray.visibility = View.GONE
            view.chip_webrip.isChecked = true
            view.chip_webrip.isClickable = false
        }
        if (webrip.size<=0) {
            view.chip_webrip.visibility = View.GONE
            view.chip_blueray.isChecked = true
            view.chip_blueray.isClickable = false
        }

        if (webrip.size>0 && bluray.size>0)
            view.chip_blueray.isChecked = true


        view.chip_blueray.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) view.chip_webrip.isChecked = false
            filterChips(view)
        }

        view.chip_webrip.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) view.chip_blueray.isChecked = false
            filterChips(view)
        }

        filterChips(view)

        /** Special code for watch now button & Library Button */
        setUpForWatch(view)

        return view
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

    private fun filterChips(view: View) {
        if (view.chip_blueray.isChecked) {
            adapter = DownloadAdapter(context,bluray)
        }else if (view.chip_webrip.isCheckable) {
            adapter = DownloadAdapter(context,webrip)
        }

        adapter.setDownloadClickListener(object: DownloadAdapter.DownloadClickListener {
            override fun onClick(torrent: Torrent, pos: Int) {
                when (tag) {
                    "watch_now" -> {
                        val i = Intent(context,
                            TorrentPlayerActivity::class.java)
                        i.putExtra("torrentLink",torrent.url)

                        if (::singleAdapter.isInitialized) {
                            singleAdapter.models.forEach {
                                if (it.isChecked) {
                                    i.putExtra("sub",it.text)
                                }
                            }
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

        adapter.setDownloadLongClickListener(object: DownloadAdapter.DownloadLongClickListener {
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

        view.recyclerView_download.adapter = adapter
    }

    private fun setUpForWatch(view: View) {
        if (tag == "watch_now") {
            view.item_tip_text.visibility = View.GONE

            /** Show subtitles */

            showSubtitle(view)
        }
    }

    private fun showSubtitle(view: View) {
        if (SUBTITLE_LOCATION.listFiles()?.isNotEmpty()!!) {

            /** Filter subtitle to check whether subtile for current movie exist */

            val titleSpan = title.split(" ")[0]

            val onlySuchFiles = SUBTITLE_LOCATION.list()?.filter { f -> f?.contains(titleSpan)!! }
            if (onlySuchFiles?.isNotEmpty()!!) {
                val cssView = LayoutInflater.from(context)
                    .inflate(R.layout.custom_select_subtitle, view.addLayout)
                val recyclerView = cssView.findViewById<RecyclerView>(R.id.recyclerView)

                val list = ArrayList<SelectSubtitle>()
                onlySuchFiles.mapTo(list) { SelectSubtitle(it) }

                singleAdapter = SelectSubAdapter(context!!, list)
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
        val subView = LayoutInflater.from(context).inflate(R.layout.custom_small_tip,view.addLayout)
        subView.tip_text.text = getString(R.string.noSubs)
        subView.tip_button.text = getString(R.string.download)
        subView.tip_button.setOnClickListener {
            val bottomSheetSubtitles =
                BottomSheetSubtitles()
            bottomSheetSubtitles.show(activity?.supportFragmentManager!!,imdbCode)
            dismiss()
        }
    }
}
