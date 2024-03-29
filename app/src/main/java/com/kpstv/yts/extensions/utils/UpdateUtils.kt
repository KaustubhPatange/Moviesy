package com.kpstv.yts.extensions.utils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.room.Update
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.kpstv.common_moviesy.extensions.Coroutines
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.yts.AppInterface
import com.kpstv.yts.BuildConfig
import com.kpstv.yts.R
import com.kpstv.yts.data.converters.AppDatabaseConverter
import com.kpstv.yts.data.models.AppDatabase
import com.kpstv.yts.data.models.Release
import com.kpstv.yts.databinding.CustomPurchaseDialogBinding
import com.kpstv.yts.extensions.SimpleCallback
import com.kpstv.yts.interfaces.api.ReleaseApi
import com.kpstv.yts.receivers.CommonBroadCast
import com.kpstv.yts.services.UpdateWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val releaseApi: ReleaseApi,
    private val retrofitUtils: RetrofitUtils
) {
    /**
     * To make this suspend worker run on non suspendable method
     * we use a callback function.
     */
    fun check(
        onUpdateAvailable: (Release) -> Unit,
        onVersionDeprecated: SimpleCallback,
        onUpdateNotFound: SimpleCallback? = null,
        onError: (Exception) -> Unit
    ) =
        Coroutines.io {
            try {
                val updatePair = fetchUpdateDetails()
                when {
                    updatePair.second -> {
                        Coroutines.main { onUpdateAvailable.invoke(updatePair.first) }
                    }
//                    updatePair.first.update.deprecatedVersionCode == BuildConfig.VERSION_CODE -> Coroutines.main { onVersionDeprecated.invoke() }
                    else -> Coroutines.main { onUpdateNotFound?.invoke() }
                }
            } catch (e: Exception) {
                Coroutines.main { onError(e) }
            }
        }

    suspend fun checkAsync(): Pair<Release, Boolean> {
        val details = fetchUpdateDetails()
        return Pair(details.first, details.second)
    }

    fun processUpdate(update: Release, lifecycleOwner: LifecycleOwner, rootView: View) = with(context) {
        val uniqueId = UpdateWorker.schedule(applicationContext, update.assets.firstOrNull { it.name.endsWith(".apk") }?.browserDownloadUrl ?: "")
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(uniqueId).observe(lifecycleOwner) { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val fileString = workInfo.outputData.getString(UpdateWorker.OUTPUT_FILE_PATH) ?: return@observe

                var snackbar: Snackbar? = null
                snackbar = Snackbar.make(rootView, getString(R.string.update_download_complete), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.update_install_button)) {
                        val intent = CommonBroadCast.getInstallApkIntent(this, fileString)
                        sendBroadcast(intent)
                        snackbar?.dismiss()
                    }
                snackbar.show()
            }
        }
    }

    private suspend fun fetchUpdateDetails(): Pair<Release, Boolean> {
        val release = releaseApi.fetchRelease()

        val newVersion = release.tagName.replace("v", "").replace(".","").toFloat()
        val currentVersion = BuildConfig.VERSION_NAME.replace(".","").toFloat()

        return Pair(release, newVersion > currentVersion)
    }

    fun showUpdateDialog(context: Context, doOnUpdateClick: SimpleCallback): Unit =
        with(context) {
            var alertDialog: AlertDialog? = null
            val binding = CustomPurchaseDialogBinding.inflate(LayoutInflater.from(this))

            binding.lottieView.setAnimation(R.raw.rocket)
            binding.lottieView.repeatCount = 0
            binding.title.text = getString(R.string.update_title)
            binding.message.text = getString(R.string.update_text)
            binding.btnDetails.text = getText(R.string.alright)
            binding.btnNeutral.text = getString(R.string.changes)
            binding.btnNeutral.show()

            binding.btnNeutral.setOnClickListener {
                AppUtils.launchUrlIntent(this, "${getString(R.string.app_github)}/releases")
            }
            binding.btnClose.setOnClickListener { alertDialog?.dismiss() }
            binding.btnDetails.setOnClickListener {
                doOnUpdateClick.invoke()
                alertDialog?.dismiss()
            }

            alertDialog = AlertDialog.Builder(this)
                .setView(binding.root)
                .create()
            alertDialog.show()
        }
}