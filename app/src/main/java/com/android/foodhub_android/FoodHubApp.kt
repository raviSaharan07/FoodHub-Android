package com.android.foodhub_android

import android.app.Application
import com.android.foodhub_android.notification.FoodHubNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FoodHubApp : Application() {

    @Inject
    lateinit var foodHubNotificationManager: FoodHubNotificationManager

    override fun onCreate() {
        super.onCreate()
        foodHubNotificationManager.createChannels()
        foodHubNotificationManager.getAndStoreToken()
    }
}