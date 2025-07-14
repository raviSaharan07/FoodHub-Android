package com.android.foodhub_android.data.models

data class CartResponse(
    val checkoutDetails: CheckoutDetails,
    val items: List<CartItem>
)
