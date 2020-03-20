package com.kpstv.yts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kpstv.yts.R
import com.kpstv.yts.models.Torrent
import kotlinx.android.synthetic.main.item_torrent_download_1.view.*

class JobQueueAdapter(val context: Context, val models: ArrayList<Torrent>) :
    RecyclerView.Adapter<JobQueueAdapter.JobHolder>() {

    private lateinit var listener: CloseClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobHolder {
        return JobHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_torrent_download_1, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: JobHolder, i: Int) {
        val model = models[i]

        holder.title.text = model.title
        Glide.with(context.applicationContext).load(model.banner_url).into(holder.image)

        holder.closeImage.setOnClickListener {
            listener.onClick(model, i)
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    interface CloseClickListener {
        fun onClick(model: Torrent, pos: Int)
    }

    fun setCloseClickListener(listener: CloseClickListener) {
        this.listener = listener
    }

    class JobHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.item_image
        val title = view.item_title
        val progressBar = view.item_progressBar
        val closeImage = view.item_close
    }

}