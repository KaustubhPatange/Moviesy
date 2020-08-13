package com.kpstv.yts.extensions.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/** I was bored of inflating view in BottomSheet by overriding
 *  [onCreateView] as I was amazed by the fact how fragment has a
 *  such constructor view injection.
 *
 *  This class will help to achieve it.
 */
open class ExtendedBottomSheetDialogFragment(
    @LayoutRes private val layoutId: Int
) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }
}