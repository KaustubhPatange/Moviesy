package com.kpstv.yts.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getBulletSymbol
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getSizePretty
import kotlinx.android.synthetic.main.item_library_download.view.*
import java.io.File


class LibraryDownloadAdapter(
    private var models: List<Model.response_download>
) : RecyclerView.Adapter<LibraryDownloadAdapter.LDHolder>() {

    lateinit var OnClickListener: (Model.response_download, Int) -> Unit
    lateinit var OnMoreClickListener: (View, Model.response_download, Int) -> Unit

    class LDHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LDHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_library_download, parent, false
            )
        )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LDHolder, i: Int) {
        val model = models[i]

        holder.itemView.item_title.text = model.title

        val bannerImagePath = File(model.imagePath!!)
        if (bannerImagePath.exists()) {
            val bitmap = BitmapFactory.decodeFile(bannerImagePath.absolutePath)
            holder.itemView.shimmerImageView.setImage(bitmap)
        }
        holder.itemView.item_subText.text =
            "${getSizePretty(model.size)} ${getBulletSymbol()} ${model.total_video_length / (1000 * 60)} mins"

        if (model.recentlyPlayed)
            holder.itemView.smallLabel.show()
        else
            holder.itemView.smallLabel.hide()

        holder.itemView.mainLayout.setOnClickListener {
            OnClickListener.invoke(model, i)
        }

        holder.itemView.item_moreButton.setOnClickListener {
            OnMoreClickListener.invoke(
                holder.itemView.item_moreButton,
                model,
                i
            )
        }
    }

    fun updateModels(models: List<Model.response_download>) {
        this.models = models;
        notifyDataSetChanged()
    }

    override fun getItemCount() = models.size
}
