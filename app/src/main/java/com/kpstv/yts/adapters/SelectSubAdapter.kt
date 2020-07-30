package com.kpstv.yts.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.yts.R
import com.kpstv.yts.data.models.SelectSubtitle
import com.kpstv.yts.interfaces.listener.SingleClickListener
import kotlinx.android.synthetic.main.item_single.view.*

class SelectSubAdapter(
    val models: List<SelectSubtitle>
):
    RecyclerView.Adapter<SelectSubAdapter.SingleListHolder>() {

    private lateinit var singleClickListener: SingleClickListener

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
        holder.layout.setOnClickListener { singleClickListener.onClick(model,i) }
    }

    fun setOnClickListener(singleClickListener: SingleClickListener) {
        this.singleClickListener = singleClickListener
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