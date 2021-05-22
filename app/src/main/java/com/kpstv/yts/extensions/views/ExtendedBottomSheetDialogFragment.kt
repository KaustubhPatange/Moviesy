package com.kpstv.yts.extensions.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.common_moviesy.extensions.transparentNavigationBar
import com.kpstv.common_moviesy.extensions.utils.CommonUtils
import com.kpstv.sheets.RoundedBottomSheetDialogFragment
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.ui.helpers.ThemeHelper

/** I was bored of inflating view in BottomSheet by overriding
 *  [onCreateView] as I was amazed by the fact how fragment has a
 *  such constructor view injection.
 *
 *  This class will help to achieve it.
 */
open class ExtendedBottomSheetDialogFragment(
    @LayoutRes private val layoutId: Int
) : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.navigationBarColor = CommonUtils.getColorFromAttr(
            requireActivity(), R.attr.colorForeground
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().transparentNavigationBar()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }
}

open class ExtendedRoundBottomSheetDialogFragment(
    @LayoutRes private val layoutId: Int
) : RoundedBottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }
}