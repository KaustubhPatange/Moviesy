package com.kpstv.yts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.common_moviesy.extensions.drawableFrom
import kotlinx.android.synthetic.main.item_search_suggestion.view.*

data class HistoryModel(
    val query: String,
    val type: Type
) {
    enum class Type {
        HISTORY,
        SEARCH
    }
}

class SearchAdapter(
    private val context: Context,
    private val list: ArrayList<HistoryModel>,
    private val onClick: (HistoryModel, Int) -> Unit,
    private val onLongClick: (HistoryModel, Int) -> Unit
) :
    RecyclerView.Adapter<SearchAdapter.SearchHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SearchHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_search_suggestion,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: SearchHolder, i: Int) {
        val item = list[i]

        holder.title.text = item.query

        when (item.type) {
            HistoryModel.Type.HISTORY ->
                holder.image.setImageDrawable(context.drawableFrom(R.drawable.ic_restore))
            HistoryModel.Type.SEARCH ->
                holder.image.setImageDrawable(context.drawableFrom(R.drawable.ic_search))
        }

        holder.mainLayout.setOnClickListener { onClick.invoke(item, i) }
        holder.mainLayout.setOnLongClickListener { onLongClick.invoke(item, i); true }
    }

    override fun getItemCount() = list.size

    class SearchHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mainLayout = view.mainLayout
        val title = view.item_text
        val image = view.item_image
    }
}