package com.kpstv.yts.extensions.common

import android.view.LayoutInflater
import android.view.ViewGroup
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.databinding.CustomTipLayoutBinding
import com.kpstv.yts.defaultPreference

class CustomTipLayout {
    data class Builder(private val view: ViewGroup) {
        val binding = CustomTipLayoutBinding.inflate(LayoutInflater.from(view.context), view, false)
        private val preference by view.context.defaultPreference()

        init {
            binding.root.hide()
        }

        fun setTitle(value: String): Builder {
            binding.title.text = value
            return this
        }

        fun setMessage(value: String): Builder {
            binding.message.text = value
            return this
        }

        fun show(prefId: String) {
            binding.btnClose.setOnClickListener {
                preference.writeBoolean(prefId, false)
                view.removeView(binding.root)
            }
            view.addView(binding.root)

            val toShow = preference.getBoolean(prefId, true)
            if (toShow)
                binding.root.show()
        }
    }
}