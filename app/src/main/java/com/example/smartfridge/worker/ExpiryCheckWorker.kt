package com.example.smartfridge.worker

import android.content.Context
import androidx.work.*
import com.example.smartfridge.data.local.AppDatabase
import com.example.smartfridge.domain.model.ExpiryStatus
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val inStockItems = db.foodItemDao().getFoodItemsByStatus(ItemActionState.IN_FRIDGE).first()

        val redItems = inStockItems.filter { it.getExpiryStatus() == ExpiryStatus.RED }

        if (redItems.isNotEmpty()) {
            NotificationHelper.sendExpiryNotification(applicationContext, redItems)
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_expiry_check_work"

        fun scheduleDailyCheck(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
