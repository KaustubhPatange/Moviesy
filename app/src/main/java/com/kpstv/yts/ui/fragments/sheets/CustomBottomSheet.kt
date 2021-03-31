package com.kpstv.yts.ui.fragments.sheets

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomDialogLayoutBinding
import com.kpstv.yts.extensions.views.ExtendedRoundBottomSheetDialogFragment

class CustomBottomSheet : ExtendedRoundBottomSheetDialogFragment(R.layout.custom_dialog_layout) {
    private val binding by viewBinding(CustomDialogLayoutBinding::bind)

    override fun getTheme(): Int = R.style.AppBottomSheetStyle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lottieView.hide()
        binding.title.text = arguments?.getString(ARG_TITLE)
        binding.subtitle.text = HtmlCompat.fromHtml(
            arguments?.getString(ARG_SUBTITLE)!!, HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.subtitle.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        binding.subtitle.movementMethod = LinkMovementMethod.getInstance()

        if (arguments?.getBoolean(ARG_SHOW_NEG) == false) {
            binding.btnNegative.hide()
        }

        binding.btnPositive.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_SUBTITLE = "arg_subtitle"
        private const val ARG_SHOW_NEG = "arg_show_neg"

        fun show(
            fragmentManager: FragmentManager,
            title: String,
            subtitle: String,
            showNegativeButton: Boolean = false
        ) {
            CustomBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                    putBoolean(ARG_SHOW_NEG, showNegativeButton)
                }
            }.show(fragmentManager, null)
        }
    }
}