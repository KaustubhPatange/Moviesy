package com.kpstv.yts.ui.fragments.sheets

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.purchase.Options
import com.kpstv.purchase.PurchaseHelper
import com.kpstv.yts.AppInterface
import com.kpstv.yts.R
import com.kpstv.yts.databinding.BottomSheetPurchaseBinding
import com.kpstv.yts.extensions.views.ExtendedBottomSheetDialogFragment
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.invisible
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.databinding.CustomTransactionLayoutBinding
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.helpers.PremiumHelper
import com.kpstv.yts.ui.helpers.SignInHelper
import com.kpstv.yts.ui.helpers.ThemeHelper
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            signInHelper.signIn(
                onSignInComplete = { account ->
                    binding.purchaseLayout.invisible()
                    binding.customMultiplePurchaseLayout.hide()
                    binding.progressLayout.show()

                    checkout(account)
                }
            )
        }
        binding.customMultiplePurchase.buttonGpay.setOnClickListener {
            AppUtils.launchUrl(
                requireContext(),
                getString(R.string.gpay_payment_url),
                ThemeHelper.isDarkVariantTheme()
            )
            dismiss()
        }
        binding.customMultiplePurchase.buttonRestore.setOnClickListener {
            signInHelper.signIn(
                onSignInComplete = { account ->
                    setCustomTransactionLayout(account.email!!, account.id!!)

                    lifecycleScope.launch {
                        binding.progressLayout.show()
                        binding.customMultiplePurchase.root.invisible()

                        val result = PurchaseHelper.checkIfUserAlreadyExist(account.email!!, account.id!!)

                        signInHelper.signOut()

                        if (result) {
                            proceedRequest()
                        } else {
                            Toasty.error(requireContext(), getString(R.string.no_premium_found)).show()

                            // Show transaction Layout
                            binding.customTransactionLayout.root.show()
                            binding.customMultiplePurchase.root.hide()
                            binding.progressLayout.hide()
                        }
                    }
                }
            )
        }
        binding.customMultiplePurchase.helpButton.setOnClickListener {
            AppUtils.launchUrlIntent(
                requireContext(),
                getString(R.string.payment_help_url)
            )
        }
    }

    private fun setCustomTransactionLayout(email: String, accountId: String) = with(binding.customTransactionLayout) {
        btnClose.setOnClickListener { dismiss() }
        btnProceed.setOnClickListener {
            binding.customTransactionLayout.root.invisible()
            binding.progressLayout.show()

            val transactionId = etTransactionId.text?.toString() ?: ""
            val isMatched = "[\\d\\w]{17}".toRegex().matches(transactionId)
            if (transactionId.isNotEmpty() && isMatched) {
                lifecycleScope.launch {
                    val result = PurchaseHelper.verifyAndActiveUser(transactionId, email, accountId)
                    result.fold(
                        onSuccess = { proceedRequest() },
                        onFailure = { error ->
                            Toasty.error(requireContext(), error.message ?: "Unknown error").show()
                            dismiss()
                        }
                    )
                }
            } else {
                Toasty.error(requireContext(), getString(R.string.error_transaction_id)).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        signInHelper.handleSignInRequest(requestCode, data)
        if (requestCode == PurchaseHelper.PURCHASE_CLIENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Handler().postDelayed({
                    proceedRequest()
                }, 1000)
            } else {
                val message = data?.getStringExtra(PurchaseHelper.ERROR_EXTRA) ?: "Cancelled"
                Toasty.warning(requireContext(), message).show()
                dismiss()
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
                    proceedRequest()
                },
                onError = {
                    Toasty.error(requireContext(), "Failed: ${it.message}").show()
                    dismiss()
                }
            )
    }

    private fun proceedRequest() {
        PremiumHelper.activatePurchase(requireContext())
        binding.lottiePurchaseComplete.setAnimation(R.raw.check)
        binding.lottiePurchaseComplete.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                PremiumHelper.showPremiumActivatedDialog(requireContext())
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
            .build()
        signInHelper.init()
    }
}