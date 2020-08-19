package com.kpstv.yts.extensions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.drawableFrom
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ItemNavigationBinding

data class NavigationModel(
    val tag: String,
    val title: String,
    @DrawableRes val drawableRes: Int,
    var notificationCount: Int = 0
)

class Navigations(
    private val context: Context
) {

    private lateinit var adapter: NavigationAdapter

    fun setUp(
        recyclerView: RecyclerView,
        models: NavigationModels,
        onSingleClick: AdapterOnSingleClick<NavigationModel>
    ) = with(context) {
        adapter = NavigationAdapter(
            models = models,
            onSingleClick = onSingleClick
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }

    fun updateNotification(tag: String, count: Int) {
        adapter.models.find { it.tag == tag }?.notificationCount = count
        adapter.notifyDataSetChanged()
    }
}

typealias NavigationModels = ArrayList<NavigationModel>

class NavigationAdapter(
    val models: NavigationModels,
    private val onSingleClick: AdapterOnSingleClick<NavigationModel>
) : RecyclerView.Adapter<NavigationAdapter.NavigationHolder>() {

    class NavigationHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NavigationHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_navigation, parent, false)
        )

    override fun getItemCount() = models.size

    override fun onBindViewHolder(holder: NavigationHolder, position: Int) {
        val binding = ItemNavigationBinding.bind(holder.itemView)

        val item = models[position]

        if (item.notificationCount > 0)
            binding.tvBadge.show()
        else
            binding.tvBadge.hide()

        binding.tvTitle.text = item.title
        binding.tvBadge.text = item.notificationCount.toString()
        binding.imageView.setImageDrawable(binding.root.context.drawableFrom(item.drawableRes))

        binding.root.setOnClickListener { onSingleClick.invoke(item, position) }
    }
}