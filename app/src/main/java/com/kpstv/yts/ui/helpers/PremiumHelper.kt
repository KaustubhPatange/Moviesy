package com.kpstv.yts.ui.helpers

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.ui.fragments.sheets.BottomSheetPurchase
import es.dmoral.toasty.Toasty

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

        /**
         * Displays a toast and then open the purchase bottom sheet
         */
        fun showDownloadPremium(activity: FragmentActivity) {
            Toasty.warning(activity, "Buy premium to remove download limit").show()
            openPurchaseFragment(activity)
        }

        fun wasPurchased(context: Context) = with(context) {
            PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(AppSettings.PREMIUM_PURCHASE_PREF, false)
        }

        fun activatePurchase(context: Context) = with(context) {
            PreferenceManager.getDefaultSharedPreferences(this).edit {
                    putBoolean(AppSettings.PREMIUM_PURCHASE_PREF, true)
                }
            AppInterface.IS_DARK_THEME = true
        }

        private fun openPurchaseFragment(activity: FragmentActivity) {
            val sheet = BottomSheetPurchase()
            sheet.show(activity.supportFragmentManager, "blank")
        }
    }
}