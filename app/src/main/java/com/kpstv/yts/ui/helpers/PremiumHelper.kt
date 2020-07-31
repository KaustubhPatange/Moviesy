package com.kpstv.yts.ui.helpers

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.ui.fragments.sheets.BottomSheetPurchase

class PremiumHelper {
    companion object {
        fun insertSubtitlePremiumTip(
            activity: FragmentActivity,
            root: ViewGroup,
            onPurchaseClick: SimpleCallback? = null
        ) =
            with(activity) {
                CommonTipHelper.Builder(this)
                    .setTitle("Buy premium to cast subtitle")
                    .setButtonText("Unlock")
                    .setParentLayout(root)
                    .setButtonClickListener {
                        onPurchaseClick?.invoke()
                        openPurchaseFragment(activity)
                    }
                    .build()
                    .populateView()
            }

        private fun openPurchaseFragment(activity: FragmentActivity) {
            val sheet = BottomSheetPurchase()
            sheet.show(activity.supportFragmentManager, "blank")
        }
    }
}