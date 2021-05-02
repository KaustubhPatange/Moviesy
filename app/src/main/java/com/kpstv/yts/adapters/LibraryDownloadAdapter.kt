package com.kpstv.yts.adapters

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.utils.CommonUtils
import com.kpstv.yts.R
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.databinding.ItemLibraryDownloadBinding
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getBulletSymbol
import java.io.File

class LibraryDownloadAdapter(
    private val onClickListener: (Model.response_download, Int) -> Unit,
    private val onMoreClickListener: (View, Model.response_download, Int) -> Unit
) : ListAdapter<Model.response_download, LibraryDownloadAdapter.LDHolder>(diffCallback) {

    private var highlightItemMovieId: Int = -1

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
            holder.binding.shimmerImageView.setImageBitmap(bitmap)
        }
        holder.binding.itemSubText.text =
            "${CommonUtils.getSizePretty(model.size)} ${getBulletSymbol()} ${model.total_video_length / (1000 * 60)} mins"

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

        // blink effect
        if (model.movieId == highlightItemMovieId) {
            highlightItemMovieId = -1
            blink(holder.itemView)
        }
    }

    fun highlightItem(movieId: Int) {
        val location = currentList.indexOfFirst { it.movieId == movieId }
        if (location != -1) {
            recyclerView?.smoothScrollToPosition(location)
            recyclerView?.findViewHolderForAdapterPosition(location)?.let { holder ->
                blink(holder.itemView)
            }
        } else {
            highlightItemMovieId = movieId
        }
    }

    private fun blink(view: View) {
        val drawable = view.background
        val fromColor = if (drawable is ColorDrawable) drawable.color else Color.TRANSPARENT
        ValueAnimator.ofArgb(fromColor, Color.WHITE, fromColor).apply {
            addUpdateListener {
                view.setBackgroundColor(it.animatedValue as Int)
            }
            repeatCount = 2
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    private var recyclerView: RecyclerView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
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