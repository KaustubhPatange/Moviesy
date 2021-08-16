package com.kpstv.yts.ui.helpers

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.extensions.AccountCallback
import com.kpstv.yts.extensions.ExceptionCallback


/**
 * Usage:
 * 1. Generate SignInHelper class using Builder
 * 2. Invoke [init] in onCreate
 * 3. Invoke [signIn] to start the flow
 * 4. Override onActivityResult and call [handleSignInRequest]
 */
open class SignInHelper {
    companion object {
        private val SignInErrorCodes =
            mutableMapOf(
                12501 to "Sign in cancelled",
                12502 to "Sign in currently in progress",
                12500 to "Sign in attempt didn't succeed with the current account"
            )
        const val GOOGLE_SIGNIN_REQUEST_CODE = 129
        const val DRIVE_ACCESS_REQUEST_CODE = 179
    }

    internal var fragment: Fragment? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    internal var onSignInComplete: AccountCallback? = null
    internal var onSignInFailed: ExceptionCallback? = null

    data class Builder(private val create: Int = 0) {
        private val signInHelper = SignInHelper()

        fun setParent(value: Fragment): Builder {
            signInHelper.fragment = value
            value.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    signInHelper.resetListeners()
                    super.onDestroy(owner)
                }
            })
            value.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    signInHelper.resetAll()
                    super.onDestroy(owner)
                }
            })
            return this
        }

        fun build() = signInHelper
    }

    fun init(signOut: Boolean = true, scope: List<Scope>? = null) {
        val fragment = fragment
        check(fragment != null) { }
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
        scope?.forEach { gso.requestScopes(it) }

        mGoogleSignInClient = GoogleSignIn.getClient(fragment.requireContext(), gso.build())
        if (signOut)
            signOut()
    }

    fun signOut() {
        mGoogleSignInClient?.signOut()
    }

    fun signIn(onSignInComplete: AccountCallback, onSignInFailed: ExceptionCallback = { it.printStackTrace() }) {
        this.onSignInComplete = onSignInComplete
        this.onSignInFailed = onSignInFailed
        if (fragment != null)
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
            } finally {
                resetListeners()
            }
        }
    }

    fun parseErrorCode(e: Exception): String {
        if (e is ApiException)
            return SignInErrorCodes[e.statusCode] ?: e.message ?: "Unknown Error"
        return e.message ?: "Unknown Error"
    }


    private fun signInFragment() {
        val fragment = fragment
        val mGoogleSignInClient = mGoogleSignInClient
        check(fragment != null) { }
        check(mGoogleSignInClient != null) { }

        val signInIntent = mGoogleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, GOOGLE_SIGNIN_REQUEST_CODE)
    }

    internal fun resetListeners() {
        onSignInComplete = null
        onSignInFailed = null
    }

    internal fun resetAll() {
        fragment = null
        mGoogleSignInClient = null
        resetListeners()
    }
}