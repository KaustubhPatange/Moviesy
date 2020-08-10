package com.kpstv.yts.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.databinding.ItemLibraryDownloadBinding
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getBulletSymbol
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getSizePretty
import java.io.File

class LibraryDownloadAdapter(
    private val onClickListener: (Model.response_download, Int) -> Unit,
    private val onMoreClickListener: (View, Model.response_download, Int) -> Unit
) : ListAdapter<Model.response_download, LibraryDownloadAdapter.LDHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LDHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_library_download, parent, false
            )
        )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LDHolder, i: Int) {
        val model = getItem(i)

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

    companion object {
        private val diffCallback: DiffUtil.ItemCallback<Model.response_download> =
            object : DiffUtil.ItemCallback<Model.response_download>() {
                override fun areItemsTheSame(
                    oldItem: Model.response_download,
                    newItem: Model.response_download
                ) =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: Model.response_download,
                    newItem: Model.response_download
                ) =
                    oldItem == newItem
            }
    }

    class LDHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemLibraryDownloadBinding.bind(view)
    }
}
/*
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
}*/
