package com.kpstv.yts.ui.fragments.sheets

import android.os.Bundle
import android.view.View
import com.kpstv.purchase.PurchaseHelper
import com.kpstv.yts.R
import com.kpstv.yts.databinding.BottomSheetPurchaseBinding
import com.kpstv.yts.extensions.ExtendedBottomSheetDialogFragment
import com.kpstv.common_moviesy.extensions.viewBinding

class BottomSheetPurchase : ExtendedBottomSheetDialogFragment(R.layout.bottom_sheet_purchase) {

    private val binding by viewBinding(BottomSheetPurchaseBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.purchaseButton.setOnClickListener {
            PurchaseHelper(requireActivity()).checkout()
        }
    }
}