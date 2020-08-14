package com.kpstv.yts.ui.fragments.sheets

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.purchase.Options
import com.kpstv.purchase.PurchaseHelper
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.databinding.BottomSheetPurchaseBinding
import com.kpstv.yts.extensions.views.ExtendedBottomSheetDialogFragment
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.invisible
import com.kpstv.yts.extensions.show
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.helpers.SignInHelper
import es.dmoral.toasty.Toasty


class BottomSheetPurchase : ExtendedBottomSheetDialogFragment(R.layout.bottom_sheet_purchase) {

    private val TAG = javaClass.simpleName
    private lateinit var signInHelper: SignInHelper

    private val binding by viewBinding(BottomSheetPurchaseBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (PremiumHelper.wasPurchased(requireContext())) {
            Toasty.info(requireContext(), getString(R.string.premium_already_unlock)).show()
            dismiss()
        }

        initializeSignIn()

        setMultiplePurchaseButtonClicks()

        binding.purchaseButton.setOnClickListener {
            binding.purchaseLayout.hide()
            binding.customMultiplePurchaseLayout.show()
        }
    }

    private fun setMultiplePurchaseButtonClicks() {
        binding.customMultiplePurchase.buttonPaypal.setOnClickListener {
            binding.purchaseLayout.invisible()
            binding.customMultiplePurchaseLayout.hide()
            binding.progressLayout.show()
            signInHelper.signIn()
        }
        binding.customMultiplePurchase.buttonGpay.setOnClickListener {
            AppUtils.launchUrl(
                requireContext(),
                getString(R.string.gpay_payment_url),
                AppInterface.IS_DARK_THEME
            )
            dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        signInHelper.handleSignInRequest(requestCode, data)
        if (requestCode == PurchaseHelper.PURCHASE_CLIENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Handler().postDelayed({
                    commonUnlock()
                }, 1000)
            } else {
                val message = data?.getStringExtra(PurchaseHelper.ERROR_EXTRA) ?: "Cancelled"
                Toasty.warning(requireContext(), message).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkout(account: GoogleSignInAccount) {
        PurchaseHelper.Builder(Options(account.email!!, account.id!!))
            .setContext(this)
            .build()
            .checkout(
                onUserExist = {
                    commonUnlock()
                },
                onError = {
                    Toasty.error(requireContext(), "Failed: ${it.message}").show()
                    dismiss()
                }
            )
    }

    private fun commonUnlock() {
        PremiumHelper.activatePurchase(requireContext())
        binding.lottiePurchaseComplete.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                Toasty.info(requireContext(), getString(R.string.premium_unlocked)).show()
                dismiss()
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        binding.progressLayout.hide()
        binding.lottiePurchaseComplete.show()
        binding.lottiePurchaseComplete.playAnimation()
    }

    private fun initializeSignIn() {
        signInHelper = SignInHelper.Builder()
            .setParent(this)
            .setOnSignInComplete {
                checkout(it)
            }
            .setOnSignInFailed { e ->
                Toasty.error(requireContext(), "Failed: ${signInHelper.parseErrorCode(e)}").show()
                dismiss()
            }
            .build()

        signInHelper.init()
    }
}