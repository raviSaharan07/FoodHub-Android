package com.android.foodhub_android.data.models

data class ConfirmPaymentResponse(
    val status: String,
    val requiredAction: Boolean,
    val clientSecret: String,
    val orderId: String,
    val orderStatus: String,
    val message: String
)
