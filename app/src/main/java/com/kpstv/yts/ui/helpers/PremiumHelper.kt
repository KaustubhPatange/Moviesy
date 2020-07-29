package com.kpstv.yts.ui.helpers

import android.app.Activity
import android.view.ViewGroup

class PremiumHelper {
    companion object {
        fun insertSubtitlePremiumTip(activity: Activity, root: ViewGroup) = with (activity) {
            CommonTipHelper.Builder(this)
                .setTitle("Buy premium to cast subtitle")
                .setButtonText("Unlock")
                .setParentLayout(root)
                .setButtonClickListener {

                }
                .build()
                .populateView()
        }
    }
}