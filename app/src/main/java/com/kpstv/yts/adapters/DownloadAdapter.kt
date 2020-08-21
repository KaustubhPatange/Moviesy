package com.kpstv.yts.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.databinding.ItemDownloadBinding
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.ui.fragments.sheets.BottomSheetDownload

@SuppressLint("SetTextI18n")
class DownloadAdapter(
    private val models: ArrayList<Torrent>,
    private val viewType: BottomSheetDownload.ViewType
) :
    RecyclerView.Adapter<DownloadAdapter.DownloadHolder>() {

    var onPremiumItemClicked: SimpleCallback? = null

    private lateinit var listener: DownloadClickListener
    private lateinit var longListener: DownloadLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHolder {
        return DownloadHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DownloadHolder, i: Int) {
        val model = models[i]
        holder.binding.itemQuality.text = model.quality
        holder.binding.itemSeeds.text = "${model.seeds} seeds"
        holder.binding.itemPeers.text = "${model.peers} peers"
        holder.binding.itemSize.text = model.size_pretty

        holder.binding.root.setOnClickListener {
            performPremiumCheck(
                model,
                { onPremiumItemClicked?.invoke() },
                { listener.onClick(model, i) })
        }
        holder.binding.root.setOnLongClickListener {
            longListener.onLongClick(model, i)
            return@setOnLongClickListener true
        }

        /** Set up some premium methods */
        performPremiumCheck(
            model,
            { holder.binding.ivPremium.show() },
            { holder.binding.ivPremium.hide() }
        )
    }

    override fun getItemCount(): Int {
        return models.size
    }

    interface DownloadClickListener {
        fun onClick(torrent: Torrent, pos: Int)
    }

    interface DownloadLongClickListener {
        fun onLongClick(torrent: Torrent, pos: Int)
    }

    fun setDownloadClickListener(listener: DownloadClickListener) {
        this.listener = listener
    }

    fun setDownloadLongClickListener(longListener: DownloadLongClickListener) {
        this.longListener = longListener
    }

    private fun performPremiumCheck(
        model: Torrent,
        doOnPremium: SimpleCallback,
        doNotOnPremium: SimpleCallback
    ) {
        if (AppInterface.IS_PREMIUM_UNLOCKED) {
            doNotOnPremium.invoke()
            return
        }
        if (viewType == BottomSheetDownload.ViewType.WATCH && (model.quality == "1080p" || model.quality == "2160p")) {
            doOnPremium.invoke()
        } else
            doNotOnPremium.invoke()
    }

    class DownloadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemDownloadBinding.bind(itemView)
    }
}