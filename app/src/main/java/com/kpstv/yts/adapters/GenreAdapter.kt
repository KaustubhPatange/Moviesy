package com.kpstv.yts.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import kotlinx.android.synthetic.main.item_genre.view.*

class GenreAdapter(val con: Context, private val models: ArrayList<String>) :
    RecyclerView.Adapter<GenreAdapter.GenreHolder>() {

    private lateinit var listener: OnClickListener;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return GenreHolder(view)
    }

    override fun onBindViewHolder(holder: GenreHolder, position: Int) {
        holder.title.text = models[position]
        holder.title.setOnClickListener { listener.onClick(models[position], position) }
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
        val title: TextView = itemView.item_genre_title
    }
}