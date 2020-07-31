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
import com.kpstv.yts.databinding.ItemLibraryDownloadBinding
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getBulletSymbol
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getSizePretty
import java.io.File


class LibraryDownloadAdapter(
    private var models: List<Model.response_download>
) : RecyclerView.Adapter<LibraryDownloadAdapter.LDHolder>() {

    lateinit var onClickListener: (Model.response_download, Int) -> Unit
    lateinit var onMoreClickListener: (View, Model.response_download, Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LDHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_library_download, parent, false
            )
        )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LDHolder, i: Int) {
        val model = models[i]

        holder.binding.itemTitle.text = model.title

        val bannerImagePath = File(model.imagePath!!)
        if (bannerImagePath.exists()) {
            val bitmap = BitmapFactory.decodeFile(bannerImagePath.absolutePath)
            holder.binding.shimmerImageView.setImage(bitmap)
        }
        holder.binding.itemSubText.text =
            "${getSizePretty(model.size)} ${getBulletSymbol()} ${model.total_video_length / (1000 * 60)} mins"

        if (model.recentlyPlayed)
            holder.binding.smallLabel.show()
        else
            holder.binding.smallLabel.hide()

        holder.binding.root.setOnClickListener {
            onClickListener.invoke(model, i)
        }

        holder.binding.itemMoreButton.setOnClickListener {
            onMoreClickListener.invoke(
                holder.binding.itemMoreButton,
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

    class LDHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemLibraryDownloadBinding.bind(view)
    }
}
