package com.kpstv.yts.ui.epoxy

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomTipLayoutBinding
import com.kpstv.yts.extensions.SimpleCallback

@EpoxyModelClass(layout = R.layout.custom_tip_layout)
abstract class CustomTipModel : EpoxyModelWithHolder<CustomTipModel.Holder>() {

    @field:EpoxyAttribute
    open lateinit var title: String

    @field:EpoxyAttribute
    open lateinit var message: String

    @field:EpoxyAttribute
    open var buttonText: String? = null

    @field:EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    open var clickListener: SimpleCallback? = null

    override fun bind(holder: Holder) = with(holder) {
        binding.title.text = title
        binding.message.text = message
        if (buttonText != null) {
            binding.btnClose.text = buttonText
        }
        if (clickListener != null) {
            binding.btnClose.setOnClickListener { clickListener?.invoke() }
        }
    }

    inner class Holder : EpoxyHolder() {
        lateinit var binding: CustomTipLayoutBinding
            private set
        override fun bindView(itemView: View) {
            binding = CustomTipLayoutBinding.bind(itemView)
        }
    }
}