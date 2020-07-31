package com.kpstv.yts.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.data.models.Torrent
import com.kpstv.yts.databinding.ItemDownloadBinding

@SuppressLint("SetTextI18n")
class DownloadAdapter(
    private val context: Context?,
    private val models: ArrayList<Torrent>
) :
    RecyclerView.Adapter<DownloadAdapter.DownloadHolder>() {

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
            listener.onClick(model, i)
        }
        holder.binding.root.setOnLongClickListener {
            longListener.onLongClick(model, i)
            return@setOnLongClickListener true
        }
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

    class DownloadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemDownloadBinding.bind(itemView)
    }
}