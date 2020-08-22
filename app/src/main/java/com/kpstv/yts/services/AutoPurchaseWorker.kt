package com.kpstv.yts.services

import android.content.Context
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.kpstv.purchase.PurchaseHelper
import com.kpstv.yts.AppInterface
import com.kpstv.yts.ui.helpers.PremiumHelper
import org.json.JSONObject
import java.util.*

/**
 * Purpose of this worker is to find if the user has already subscribed
 * to premium but app is not activated, in such case it will auto
 * activate it.
 *
 * This worker fails sometimes, because of the Write external storage
 */
class AutoPurchaseWorker @WorkerInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!AppInterface.IS_PREMIUM_UNLOCKED) {
            val purchaseJSON = PremiumHelper.getPurchaseHistoryJSON()
            if (purchaseJSON != null) {
                val jsonObject = JSONObject(purchaseJSON)
                val email = jsonObject.getString("email")
                val accountId = jsonObject.getString("uid")
                val userExist =
                    PurchaseHelper.checkIfUserAlreadyExist(email, accountId)
                if (userExist) {
                    return Result.success(workDataOf(IS_PURCHASE_ACTIVATED to true))
                }
            }
        }
        return Result.success(workDataOf(IS_PURCHASE_ACTIVATED to false))
    }

    companion object {
        private const val UNIQUE_WORK_ID = "moviesy_auto_worker"
        const val IS_PURCHASE_ACTIVATED = "auto_worker_purchase_activated"
        fun schedule(context: Context): UUID {
            val request = OneTimeWorkRequestBuilder<AutoPurchaseWorker>()
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_ID, ExistingWorkPolicy.KEEP, request)

            return request.id
        }
    }
}