package com.android.foodhub_android.notification

import android.app.PendingIntent
import android.content.Intent
import com.android.foodhub_android.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FoodHubMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var foodHubNotificationManager: FoodHubNotificationManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        val title = message.notification?.title ?: ""
        val messageText = message.notification?.body ?: ""
        val data = message.data
        val type = data["type"] ?: "general"

        if (type == "order") {
            val orderID = data[ORDER_ID]
            intent.putExtra(ORDER_ID, orderID)
        }
        val notificationChannelType = when (type) {
            "order" -> FoodHubNotificationManager.NotificationChannelType.ORDER
            "general" -> FoodHubNotificationManager.NotificationChannelType.PROMOTION
            else -> FoodHubNotificationManager.NotificationChannelType.ACCOUNT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        foodHubNotificationManager.showNotification(
            title,
            messageText,
            1,
            pendingIntent,
            notificationChannelType
        )

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        foodHubNotificationManager.updateToken(token)
    }

    companion object {
        const val ORDER_ID = "orderId"
    }
}