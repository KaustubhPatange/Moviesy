package com.kpstv.yts.ui.helpers

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kpstv.yts.extensions.AccountCallback
import com.kpstv.yts.extensions.ExceptionCallback
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.ui.fragments.sheets.BottomSheetPurchase

/**
 * Usage:
 * 1. Generate SignInHelper class using Builder
 * 2. Invoke [init] in onCreate
 * 3. Invoke [signIn] to start the flow
 * 4. Override onActivityResult and call [handleSignInRequest]
 */
class SignInHelper {
    companion object {
        const val GOOGLE_SIGNIN_REQUEST_CODE = 129
    }

    private lateinit var context: Context
    private lateinit var fragment: Fragment
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var onSignInComplete: AccountCallback? = null
    private var onSignInFailed: ExceptionCallback? = null

    data class Builder(private val context: Context) {
        private val signInHelper = SignInHelper()
        init {
            signInHelper.context = context
        }
        fun setParent(value: Fragment): Builder {
            signInHelper.fragment = value
            return this
        }
        fun setOnSignInComplete(value: AccountCallback): Builder {
            signInHelper.onSignInComplete = value
            return this
        }
        fun setOnSignInFailed(value: ExceptionCallback): Builder {
            signInHelper.onSignInFailed = value
            return this
        }
        fun build() = signInHelper
    }

    fun init(signOut: Boolean = true) {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
        if (signOut)
            mGoogleSignInClient.signOut()
    }

    fun signIn() {
       if (::fragment.isInitialized)
           signInFragment()
        // TODO: Remaining stuff for Activity
    }

    fun handleSignInRequest(requestCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGNIN_REQUEST_CODE) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account =
                    task.getResult(ApiException::class.java)!!
                onSignInComplete?.invoke(account)
            } catch (e: ApiException) {
                onSignInFailed?.invoke(e)
            }
        }
    }

    private fun signInFragment() {
        val signInIntent = mGoogleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, GOOGLE_SIGNIN_REQUEST_CODE)
    }
}