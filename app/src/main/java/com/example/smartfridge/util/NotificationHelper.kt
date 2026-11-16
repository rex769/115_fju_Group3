package com.example.smartfridge.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartfridge.data.local.entity.FoodItemEntity

object NotificationHelper {

    private const val CHANNEL_ID = "food_expiry_channel"
    private const val CHANNEL_NAME = "食材到期提醒"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每天提醒快過期的冰箱食材"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission", "POST_NOTIFICATIONS")
    fun sendExpiryNotification(context: Context, expiringItems: List<FoodItemEntity>) {
        if (expiringItems.isEmpty()) return

        createNotificationChannel(context)

        val itemNames = expiringItems.joinToString("、") { it.name }
        val title = "冰箱有 ${expiringItems.size} 項食材即將到期！"
        val message = "提醒您，【${itemNames}】快要到期了，記得優先烹調處理喔！"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
