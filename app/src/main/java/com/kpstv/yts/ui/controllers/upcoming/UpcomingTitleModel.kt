package com.kpstv.yts.ui.controllers.upcoming

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ItemUpcomingTitleBinding

@EpoxyModelClass(layout = R.layout.item_upcoming_title)
abstract class UpcomingTitleModel : EpoxyModelWithHolder<UpcomingTitleModel.Holder>() {

    @field:EpoxyAttribute
    open lateinit var title: String

    override fun bind(holder: Holder) = with(holder.binding) {
        tvTitle.text = title
    }

    inner class Holder : EpoxyHolder() {
        lateinit var binding: ItemUpcomingTitleBinding
            private set
        override fun bindView(itemView: View) {
            binding = ItemUpcomingTitleBinding.bind(itemView)
        }

    }
}