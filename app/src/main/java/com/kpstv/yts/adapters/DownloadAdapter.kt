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
import com.kpstv.yts.models.Torrent
import kotlinx.android.synthetic.main.item_download.view.*

@SuppressLint("SetTextI18n")
class DownloadAdapter(val context: Context?, val models: ArrayList<Torrent>) : RecyclerView.Adapter<DownloadAdapter.DownloadHolder>() {

    private lateinit var listener: DownloadClickListener
    private lateinit var longListener: DownloadLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHolder {
        return DownloadHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_download,parent,false))
    }

    override fun onBindViewHolder(holder: DownloadHolder, i: Int) {
        val model = models[i]
        holder.title.text = model.quality
        holder.seeds.text = "${model.seeds} seeds"
        holder.peers.text = "${model.peers} peers"
        holder.size.text = model.size_pretty

        holder.mainCard.setOnClickListener {
            listener.onClick(model,i)
        }
        holder.mainCard.setOnLongClickListener {
            longListener.onLongClick(model,i)
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
        val title: TextView = itemView.item_quality
        val seeds: TextView = itemView.item_seeds
        val peers: TextView = itemView.item_peers
        val size: Button = itemView.item_size
        val mainCard: CardView = itemView.mainCard
    }

}