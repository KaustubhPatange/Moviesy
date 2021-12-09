package com.kpstv.yts.ui.epoxy.upcoming

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.R
import com.kpstv.yts.databinding.ItemUpcomingAvailableBinding
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.extensions.load

@EpoxyModelClass(layout = R.layout.item_upcoming_available)
abstract class UpcomingAvailableModel : EpoxyModelWithHolder<UpcomingAvailableModel.Holder>() {

    @field:EpoxyAttribute
    open lateinit var title: String

    @field:EpoxyAttribute
    open lateinit var imageUrl: String

    @field:EpoxyAttribute
    open var year: Int = 0

    @field:EpoxyAttribute
    open lateinit var qualityString: String

    @field:EpoxyAttribute
    open var progress: Int = 0

    @field:EpoxyAttribute
    open var rating: Double = 0.0

    @field:EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    open var comingSoonListener: SimpleCallback? = null

    @field:EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    open var clickListener: SimpleCallback? = null

    override fun bind(holder: Holder) = with(holder.binding) {
        itemTitle.text = title
        if (rating > 0) {
            itemSubText.text = root.context.getString(R.string.upcoming_subtext, year.toString(), rating.toString())
        } else {
            itemSubText.text = year.toString()
        }

        itemShimmerImage.isShimmering = true
        itemShimmerImage.load(
            uri = imageUrl,
            onSuccess = { resource ->
                itemShimmerImage.setImageBitmap(resource)
                itemShimmerImage.isShimmering = false
            }
        )

        itemQuality.text = qualityString
        itemProgress.progress = progress.toFloat()

        val comingSoonListener = comingSoonListener
        if (comingSoonListener != null) {
            btnComingSoon.show()
            itemQuality.hide()
            btnComingSoon.setOnClickListener { comingSoonListener() }
        } else {
            btnComingSoon.hide()
            itemQuality.show()
            btnComingSoon.setOnClickListener(null)
        }

        val listener = clickListener
        if (listener == null) {
            root.isEnabled = false
            root.setOnClickListener(null)
        } else {
            root.isEnabled = true
            root.setOnClickListener { listener.invoke() }
        }
    }

    inner class Holder : EpoxyHolder() {
        lateinit var binding: ItemUpcomingAvailableBinding
            private set
        override fun bindView(itemView: View) {
            binding = ItemUpcomingAvailableBinding.bind(itemView)
        }
    }
}