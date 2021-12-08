package com.kpstv.yts.ui.controllers

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomProgressBinding

@EpoxyModelClass(layout = R.layout.custom_progress)
abstract class ProgressBarModel : EpoxyModelWithHolder<ProgressBarModel.Holder>() {

    inner class Holder : EpoxyHolder() {
        lateinit var binding: CustomProgressBinding private set
        override fun bindView(itemView: View) {
            binding = CustomProgressBinding.bind(itemView)
        }
    }
}