package com.android.foodhub_android.ui.navigation

import com.android.foodhub_android.data.models.FoodItem
import kotlinx.serialization.Serializable

interface NavRoute

@Serializable
object Login: NavRoute

@Serializable
object SignUp: NavRoute

@Serializable
object AuthScreen: NavRoute

@Serializable
object Home: NavRoute

@Serializable
data class RestaurantDetails(
    val restaurantID: String,
    val restaurantName: String,
    val restaurantImageUrl: String,
): NavRoute

@Serializable
data class FoodDetails(val foodItem: FoodItem): NavRoute

@Serializable
object Cart: NavRoute

@Serializable
object Notification: NavRoute

@Serializable
object AddressList: NavRoute

@Serializable
object AddAddress: NavRoute

@Serializable
data class OrderSuccess(val orderId: String) : NavRoute

@Serializable
object OrderList: NavRoute

@Serializable
data class OrderDetails(val orderId: String) : NavRoute