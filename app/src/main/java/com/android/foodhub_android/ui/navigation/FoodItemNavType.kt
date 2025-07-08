package com.android.foodhub_android.ui.navigation

import android.os.Bundle
import androidx.navigation.NavType
import com.android.foodhub_android.data.models.FoodItem
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

val foodItemNavType = object: NavType<FoodItem>(false){
    override fun get(bundle: Bundle, key: String): FoodItem? {
        return parseValue(bundle.getString(key).toString()).copy(
            imageUrl = URLDecoder.decode(
                parseValue(bundle.getString(key).toString()).imageUrl,
                "UTF-8"
            )
        )
    }

    override fun parseValue(value: String): FoodItem {
        return Json.decodeFromString(FoodItem.serializer(),value)
    }

    override fun serializeAsValue(value: FoodItem): String {
        return Json.encodeToString(FoodItem.serializer(),value.copy(
            imageUrl = URLEncoder.encode(value.imageUrl,"UTF-8")
        ))
    }

    override fun put(bundle: Bundle, key: String, value: FoodItem) {
        bundle.putString(key,Json.encodeToString(FoodItem.serializer(),value))
    }
}