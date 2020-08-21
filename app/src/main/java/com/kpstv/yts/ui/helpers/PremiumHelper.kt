package com.kpstv.yts.ui.helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings
import com.kpstv.yts.databinding.CustomPurchaseDialogBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.ui.fragments.sheets.BottomSheetPurchase
import es.dmoral.toasty.Toasty

class PremiumHelper {
    companion object {

        private const val TO_SHOW_PURCHASE_INFO_PREF = "to_show_purchase_info_pref"

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
        fun showPremiumInfo(activity: FragmentActivity, featureName: String = "this") {
            Toasty.warning(activity, "Buy premium to remove $featureName limit").show()
            openPurchaseFragment(activity)
        }

        fun wasPurchased(context: Context) = with(context) {
            defaultPreference().value.getBoolean(AppSettings.PREMIUM_PURCHASE_PREF, false)
        }

        fun activatePurchase(context: Context) = with(context) {
            defaultPreference().value.writeBoolean(AppSettings.PREMIUM_PURCHASE_PREF, true)
            AppInterface.IS_DARK_THEME = true
        }

        fun openPurchaseFragment(activity: FragmentActivity) {
            val sheet = BottomSheetPurchase()
            sheet.show(activity.supportFragmentManager, "blank")
        }

        /** Shows a purchase dialog (if need to show) */
        fun showPurchaseInfo(activity: FragmentActivity) = with(activity) {
            if (defaultPreference().value.getBoolean(TO_SHOW_PURCHASE_INFO_PREF, true)) {
                defaultPreference().value.writeBoolean(TO_SHOW_PURCHASE_INFO_PREF, false)
                var dialog: AlertDialog? = null
                val binding = CustomPurchaseDialogBinding.inflate(LayoutInflater.from(this))
                binding.btnClose.setOnClickListener {
                    dialog?.dismiss()
                }
                binding.btnDetails.setOnClickListener {
                    openPurchaseFragment(this)
                    dialog?.dismiss()
                }

                dialog = AlertDialog.Builder(this)
                    .setView(binding.root)
                    .setCancelable(false)
                    .create()
                dialog.show()
            }
        }
    }
}