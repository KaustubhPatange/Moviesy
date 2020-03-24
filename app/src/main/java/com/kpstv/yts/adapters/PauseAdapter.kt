package com.kpstv.yts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kpstv.yts.R
import com.kpstv.yts.activities.DownloadActivity.Companion.calculateCurrentSize
import com.kpstv.yts.models.response.Model
import com.kpstv.yts.utils.AppUtils
import kotlinx.android.synthetic.main.item_torrent_download.view.*

class PauseAdapter(
    private val context: Context,
    private var models: List<Model.response_pause>
) : RecyclerView.Adapter<PauseAdapter.PauseHolder>() {

    lateinit var setOnMoreListener: (View, Model.response_pause, Int) -> Unit

    class PauseHolder(view: View) : RecyclerView.ViewHolder(view) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PauseHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_torrent_download, parent, false
            )
        )

    override fun onBindViewHolder(holder: PauseHolder, i: Int) {
        val model = models[i]

        Glide.with(context).load(model.job.bannerUrl).into(holder.itemView.item_image)

        holder.itemView.item_title.text = model.job.title
        holder.itemView.item_status.text = "Paused"
        holder.itemView.item_progress.text = "${model.job.progress}%"
        holder.itemView.item_seeds_peers.text = "0/0"
        holder.itemView.item_progressBar.progress = model.job.progress
        holder.itemView.item_current_size.text = calculateCurrentSize(model.job)
        holder.itemView.item_total_size.text = AppUtils.getSizePretty(model.job.totalSize)
        holder.itemView.item_download_speed.text = "0 KB/s"
        holder.itemView.item_more_imageView.setOnClickListener {
            setOnMoreListener.invoke(it, model, i)
        }
    }

    fun updateModels(models: List<Model.response_pause>) {
        this.models = models
        notifyDataSetChanged()
    }

    override fun getItemCount() = models.size

}