package com.android.foodhub_android.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val id: String? = null,
    val userId: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable