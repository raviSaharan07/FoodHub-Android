package com.android.foodhub_android.data.models

data class ConfirmPaymentRequest(
    val paymentIntentId: String,
    val addressId: String
)
