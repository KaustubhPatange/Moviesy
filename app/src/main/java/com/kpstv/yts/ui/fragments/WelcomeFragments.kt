package com.kpstv.yts.ui.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kpstv.common_moviesy.extensions.applyBottomInsets
import com.kpstv.common_moviesy.extensions.colorFrom
import com.kpstv.common_moviesy.extensions.globalVisibleRect
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.databinding.FragmentAgreementBinding
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.ui.activities.StartActivity
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.CircularPayload
import com.kpstv.navigation.ValueFragment
import com.kpstv.navigation.Navigator
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WelcomeArgs(
    @StringRes val message: Int,
    @StringRes val buttonNext: Int,
    @StringRes val buttonBack: Int,
    @ColorRes val backgroundColor: Int
) : BaseArgs(), Parcelable

abstract class AbstractWelcomeFragment : ValueFragment(R.layout.fragment_agreement) {
    val binding by viewBinding(FragmentAgreementBinding::bind)

    abstract fun onNextClick()

    abstract fun onBackClick()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = getKeyArgs<WelcomeArgs>()

        binding.apply {
            tvSummary.text = getString(args.message)
            btnAgree.text = getString(args.buttonNext)
            btnClose.text = getString(args.buttonBack)

            btnAgree.setTextColor(colorFrom(args.backgroundColor))
            root.setBackgroundColor(colorFrom(args.backgroundColor))

            btnAgree.applyBottomInsets()
            btnClose.applyBottomInsets()

            btnClose.setOnClickListener{
                onBackClick()
            }
            btnAgree.setOnClickListener {
                onNextClick()
            }
        }

    }
}

@AndroidEntryPoint
class WelcomeDisclaimerFragment : AbstractWelcomeFragment() {
    companion object {
        val args = WelcomeArgs(
            message = R.string.disclaimer_text,
            buttonNext = R.string.next,
            buttonBack = R.string.close,
            backgroundColor = R.color.background_agree
        )
    }

    private val navViewModel by activityViewModels<StartViewModel>()

    override fun onNextClick() {
        val errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
        if (errorCode != ConnectionResult.SUCCESS) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.play_service_error) + " (Code: $errorCode).")
                .setPositiveButton(getString(R.string.okay)) { _, _ -> moveForward() }
                .show()
        } else {
            moveForward()
        }
    }

    override fun onBackClick() {
        requireActivity().finish()
    }

    private fun moveForward() {
        navViewModel.navigateTo(
            screen = StartActivity.Screen.WELCOME_CARRIER_DISCLAIMER,
            args = WelcomeCarrierFragment.args,
            addToBackStack = true,
            transition = Navigator.TransitionType.CIRCULAR,
            transitionPayload = CircularPayload(
                fromTarget = binding.btnAgree.globalVisibleRect()
            )
        )
    }
}

@AndroidEntryPoint
class WelcomeCarrierFragment : AbstractWelcomeFragment() {
    companion object {
        val args = WelcomeArgs(
            message = R.string.disclaimer_text2,
            buttonNext = R.string.agree,
            buttonBack = R.string.back,
            backgroundColor = R.color.red
        )
    }

    private val appPreference by defaultPreference()
    private val navViewModel by activityViewModels<StartViewModel>()

    override fun onNextClick() {
        appPreference.isFirstLaunch(false)
        navViewModel.navigateTo(
            screen = StartActivity.Screen.MAIN,
            popUpTo = true,
        )
    }

    override fun onBackClick() {
        goBack()
    }
}