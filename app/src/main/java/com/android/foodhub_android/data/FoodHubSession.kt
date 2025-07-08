package com.android.foodhub_android.data

import android.content.Context

class FoodHubSession(val context: Context) {
    val sharedPres = context.getSharedPreferences("foodhub",Context.MODE_PRIVATE)

    fun storeToken(token:String){
        sharedPres.edit().putString("token",token).apply()
    }

    fun getToken(): String?{
        sharedPres.getString("token",null)?.let{
            return it
        }
        return null
    }
}