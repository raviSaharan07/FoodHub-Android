package com.android.foodhub_android.data.models

data class AddToCartRequest(
    val restaurantId: String,
    val menuItemId: String,
    val quantity: Int
)