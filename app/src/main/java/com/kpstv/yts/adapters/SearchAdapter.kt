package com.kpstv.yts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.interfaces.listener.SingleClickListener
import kotlinx.android.synthetic.main.item_search_suggestion.view.*

class SearchAdapter(private val context: Context, private val list: ArrayList<String>) :
    RecyclerView.Adapter<SearchAdapter.SearchHolder>() {

    private lateinit var listener: SingleClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SearchHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_search_suggestion,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: SearchHolder, i: Int) {
        holder.title.text = list[i]

        holder.mainLayout.setOnClickListener { listener.onClick(list[i],i) }
    }

    fun setSingleClickListener(listener: SingleClickListener) {
        this.listener = listener
    }

    override fun getItemCount() = list.size

    class SearchHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mainLayout = view.mainLayout
        val title = view.item_text
        val image = view.item_image
    }
}