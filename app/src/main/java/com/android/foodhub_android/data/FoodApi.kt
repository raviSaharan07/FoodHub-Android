package com.android.foodhub_android.data

import com.android.foodhub_android.data.models.AuthResponse
import com.android.foodhub_android.data.models.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FoodApi {

    @GET("/food")
    suspend fun getFoods(): List<String>

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): AuthResponse
}