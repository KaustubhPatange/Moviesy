package com.kpstv.yts.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ItemGenreBinding

class GenreAdapter(private val models: ArrayList<String>) :
    RecyclerView.Adapter<GenreAdapter.GenreHolder>() {

    private lateinit var listener: OnClickListener;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreHolder {
        return GenreHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_genre, parent, false)
        )
    }

    override fun onBindViewHolder(holder: GenreHolder, position: Int) {
        holder.binding.itemGenreTitle.text = models[position]
        holder.binding.itemGenreTitle.setOnClickListener {
            listener.onClick(
                models[position],
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    interface OnClickListener {
        fun onClick(text: String, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    class GenreHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemGenreBinding.bind(itemView)
    }
}