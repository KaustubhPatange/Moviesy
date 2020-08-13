package com.kpstv.yts.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.data.models.SelectSubtitle
import com.kpstv.yts.extensions.AdapterOnSingleClick
import kotlinx.android.synthetic.main.item_single.view.*

class SelectSubAdapter(
    val models: List<SelectSubtitle>
):
    RecyclerView.Adapter<SelectSubAdapter.SingleListHolder>() {

    private lateinit var adapterOnSingleClick: AdapterOnSingleClick<SelectSubtitle>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleListHolder {
        return SingleListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_single,parent,false
            )
        )
    }
    override fun onBindViewHolder(holder: SingleListHolder, i: Int) {
        val model = models[i]

        holder.checkBox.isChecked = model.isChecked

        holder.txtView.text = model.text
        holder.layout.setOnClickListener { adapterOnSingleClick.invoke(model,i) }
    }

    fun setOnClickListener(adapterOnSingleClick: AdapterOnSingleClick<SelectSubtitle>) {
        this.adapterOnSingleClick = adapterOnSingleClick
    }

    override fun getItemCount(): Int {
        return models.size
    }

    class SingleListHolder(view: View): RecyclerView.ViewHolder(view) {
        val checkBox = view.item_checkBox
        val layout = view.mainLayout
        val txtView = view.item_text
    }
}