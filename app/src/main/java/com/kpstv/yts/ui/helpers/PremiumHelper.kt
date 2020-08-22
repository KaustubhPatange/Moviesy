package com.kpstv.yts.ui.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import com.kpstv.common_moviesy.extensions.invisible
import com.kpstv.yts.AppInterface
import com.kpstv.yts.AppSettings
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomPurchaseDialogBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.services.AutoPurchaseWorker
import com.kpstv.yts.ui.fragments.sheets.BottomSheetPurchase
import es.dmoral.toasty.Toasty

@Suppress("DEPRECATION")
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
            AppInterface.IS_PREMIUM_UNLOCKED = true
            AppInterface.IS_DARK_THEME = true
        }

        fun openPurchaseFragment(activity: FragmentActivity) {
            val sheet = BottomSheetPurchase()
            sheet.show(activity.supportFragmentManager, "blank")
        }

        /** Shows a purchase dialog (if need to show) */
        fun showPurchaseInfo(activity: FragmentActivity) = with(activity) {
            if (!AppInterface.IS_PREMIUM_UNLOCKED && defaultPreference().value.getBoolean(
                    TO_SHOW_PURCHASE_INFO_PREF,
                    true
                )
            ) {
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

        @SuppressLint("SetTextI18n")
        fun showPremiumActivatedDialog(context: Context): Unit = with(context) {
            var alertDialog: AlertDialog? = null

            val binding = CustomPurchaseDialogBinding.inflate(LayoutInflater.from(this))

            binding.lottieView.setAnimation(R.raw.premium_unlocked)
            binding.lottieView.repeatCount = 0
            binding.title.text = getString(R.string.premium_unlock_text)
            binding.message.text = getString(R.string.premium_unlock_message)
            binding.btnClose.invisible()
            binding.btnDetails.text = getString(R.string.close)
            binding.btnDetails.setOnClickListener {
                alertDialog?.dismiss()
            }

            alertDialog = AlertDialog.Builder(this)
                .setView(binding.root)
                .create()
            alertDialog.show()
        }

        /** If there is purchase made from GPay, we can auto activate it */
        fun scanForAutoPurchase(
            activity: FragmentActivity,
            onPremiumActivated: SimpleCallback? = null,
            onNoPremiumFound: SimpleCallback? = null
        ) = with(activity) {
            val requestId = AutoPurchaseWorker.schedule(this)
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(requestId)
                .observe(this, Observer {
                    if (it != null && it.state.isFinished && it.outputData.getBoolean(
                            AutoPurchaseWorker.IS_PURCHASE_ACTIVATED,
                            false
                        )
                    ) {
                        activatePurchase(this)
                        onPremiumActivated?.invoke()
                    } else
                        onNoPremiumFound?.invoke()
                })
        }

        fun getPurchaseHistoryJSON(): String? {
            val pattern = AppInterface.PURCHASE_REGEX_PATTERN.toRegex()
            val fileList = Environment.getExternalStorageDirectory().listFiles()
                ?.filter { pattern.containsMatchIn(it.name) }
            return if (fileList?.isNotEmpty() == true) {
                fileList[0].readText()
            } else
                null
        }
    }
}