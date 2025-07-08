package com.android.foodhub_android.ui.features.restaurant_details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.FoodItem
import com.android.foodhub_android.data.remote.ApiResponse
import com.android.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {
    private var errorMsg = ""
    private var errorDescription = ""

    private val _uiState = MutableStateFlow<RestaurantEvent>(RestaurantEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RestaurantNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun getFoodItem(id: String){
        Log.d("RestaurantViewModel", "getFoodItem: $id")
        viewModelScope.launch{
            _uiState.value = RestaurantEvent.Loading
            val response = safeApiCall {
                foodApi.getFoodItemForRestaurant(id)
            }
            when(response){
                is ApiResponse.Success -> {
                    Log.d("RestaurantViewModel", "getFoodItem: ${response.data.foodItems}")
                    _uiState.value = RestaurantEvent.Success(response.data.foodItems)
                }
                is ApiResponse.Exception ->{
                    Log.d("RestaurantViewModel", "getFoodItem: ${response.exception}")
                    errorMsg = "Exception"
                    errorDescription = "Please try again later"
                }
                else -> {
                    Log.d("RestaurantViewModel", "getFoodItem: $response")
                    val error = (response as? ApiResponse.Error)?.code
                    when(error){
                        401 -> {
                            errorMsg = "Unauthorized"
                            errorDescription = "You are not authorized to view this page"

                        }
                        500 -> {
                            errorMsg = "Server Error"
                            errorDescription = "Please try again later. Server Error!"
                        }
                        404 -> {
                            errorMsg = "Not Found"
                            errorDescription = "Restaurant Not Found"
                        }
                        else -> {
                            errorMsg = "Unknown Error"
                            errorDescription = "Please try again later"
                        }
                    }
                    _uiState.value = RestaurantEvent.Error
                    _navigationEvent.emit(RestaurantNavigationEvent.ShowErrorDialog)
                }
            }

        }

    }

    sealed class RestaurantNavigationEvent{
        data object GoBack: RestaurantNavigationEvent()
        data object ShowErrorDialog: RestaurantNavigationEvent()
        data class NavigateToProductDetails(val productID: String): RestaurantNavigationEvent()
    }

    sealed class RestaurantEvent{
        data object Nothing: RestaurantEvent()
        data class Success(val foodItems: List<FoodItem>): RestaurantEvent()
        data object Error: RestaurantEvent()
        data object Loading: RestaurantEvent()
    }
}