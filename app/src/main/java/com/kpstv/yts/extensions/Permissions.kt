package com.kpstv.yts.extensions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.Fragment

object Permissions {

    private const val STORAGE_PERMISSION = 124

    fun verifyStoragePermission(activity: Activity, callback: SimpleCallback? = null) {
        checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION, callback)
    }

    fun verifyStoragePermission(fragment: Fragment, callback: SimpleCallback? = null) {
        checkPermission(fragment, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION, callback)
    }

    fun processStoragePermission(
        requestCode: Int,
        grantResults: IntArray,
        callback: SimpleCallback? = null
    ) {
        if (requestCode == STORAGE_PERMISSION && grantResults.isNotEmpty())
            callback?.invoke()
    }

    private fun checkPermission(
        context: Activity,
        permission: String,
        requestCode: Int,
        callback: SimpleCallback? = null
    ) = with(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(permission),
                requestCode
            )
            return@with
        }
        callback?.invoke()
    }

    private fun checkPermission(
        context: Fragment,
        permission: String,
        requestCode: Int,
        callback: SimpleCallback? = null
    ) = with(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            requireContext()
                .checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(permission),
                requestCode
            )
            return@with
        }
        callback?.invoke()
    }
}