package com.android.foodhub_android.data.models

data class OrderItem(
    val id: String,
    val menuItemId: String,
    val orderId: String,
    val quantity: Int
)