package com.kpstv.yts.ui.helpers

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
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
// TODO: Re-think this with perspective of fragment manager & remove its dependency over Fragment Activity.
class PremiumHelper {
    companion object {

        private const val TO_SHOW_PURCHASE_INFO_PREF = "to_show_purchase_info_pref"

        fun insertSubtitlePremiumTip(
            context: Context,
            fragmentManager: FragmentManager,
            root: ViewGroup,
            onPurchaseClick: SimpleCallback? = null
        ) =
            with(context) {
                CommonTipHelper.Builder(this)
                    .setTitle("Buy premium to cast subtitle")
                    .setButtonText("Unlock")
                    .setParentLayout(root)
                    .setButtonClickListener {
                        onPurchaseClick?.invoke()
                        openPurchaseFragment(fragmentManager)
                    }
                    .build()
                    .populateView()
            }

        /**
         * Displays a toast and then open the purchase bottom sheet
         */
        fun showPremiumInfo(context: Context, fragmentManager: FragmentManager, featureName: String = "this") {
            Toasty.warning(context, "Buy premium to remove $featureName limit").show()
            openPurchaseFragment(fragmentManager)
        }

        fun wasPurchased(context: Context) = with(context) {
            defaultPreference().value.getBoolean(AppSettings.PREMIUM_PURCHASE_PREF, false)
        }

        fun activatePurchase(context: Context) = with(context) {
            defaultPreference().value.writeBoolean(AppSettings.PREMIUM_PURCHASE_PREF, true)
            AppInterface.IS_PREMIUM_UNLOCKED = true
        }

        fun openPurchaseFragment(fragmentManager: FragmentManager) {
            val sheet = BottomSheetPurchase()
            sheet.show(fragmentManager, "blank")
        }

        /** Shows a purchase dialog (if need to show) */
        fun showPurchaseInfo(context: Context, fragmentManager: FragmentManager) = with(context) {
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
                    openPurchaseFragment(fragmentManager)
                    dialog?.dismiss()
                }

                dialog = AlertDialog.Builder(this)
                    .setView(binding.root)
                    .setCancelable(false)
                    .create()
                dialog.show()
            }
        }

        fun showPremiumActivatedDialog(context: Context) {
            showLottieDialog(
                context = context,
                anim = R.raw.premium_unlocked,
                title = R.string.premium_unlock_text,
                message = R.string.premium_unlock_message
            )
        }

        fun showPremiumAlreadyPurchasedDialog(context: Context) {
            showLottieDialog(
                context = context,
                anim = R.raw.heart,
                scale = 1.8f,
                title = R.string.premium_purchase_text,
                message = R.string.premium_purchase_message
            )
        }

        private fun showLottieDialog(context: Context, @RawRes anim: Int, @StringRes title: Int, @StringRes message: Int, scale: Float = 1.0f): Unit = with(context) {
            var alertDialog: AlertDialog? = null

            val binding = CustomPurchaseDialogBinding.inflate(LayoutInflater.from(this))

            binding.lottieView.setAnimation(anim)
            binding.lottieView.repeatCount = 0
            binding.lottieView.scaleX = scale
            binding.lottieView.scaleY = scale
            binding.title.text = getString(title)
            binding.message.text = getString(message)
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
            context: Context,
            lifecycleOwner: LifecycleOwner,
            onPremiumActivated: SimpleCallback? = null,
            onNoPremiumFound: SimpleCallback? = null
        ): Unit = with(context) {
            val requestId = AutoPurchaseWorker.schedule(this)
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(requestId)
                .observe(lifecycleOwner, Observer {
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