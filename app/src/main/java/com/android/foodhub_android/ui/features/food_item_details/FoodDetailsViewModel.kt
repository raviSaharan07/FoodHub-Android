package com.android.foodhub_android.ui.features.food_item_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.AddToCartRequest
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
class FoodDetailsViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel(){

    private val _uiState = MutableStateFlow<FoodDetailsUiState>(FoodDetailsUiState.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<FoodDetailsEvent>()
    val event = _event.asSharedFlow()

    private val _quantity = MutableStateFlow<Int>(1)
    val quantity = _quantity.asStateFlow()

    fun incrementQuantity(){
        if(_quantity.value == 5){
            return
        }
        _quantity.value++
    }

    fun decrementQuantity(){
        if(_quantity.value == 1){
            return
        }
        _quantity.value--
    }

    fun addToCart(restaurantId: String, foodItemId: String){
        viewModelScope.launch{
            _uiState.value = FoodDetailsUiState.Loading
            val response = safeApiCall {
                foodApi.addToCart(
                    AddToCartRequest(
                        restaurantId = restaurantId,
                        menuItemId = foodItemId,
                        quantity = quantity.value
                    )
                )
            }

            when(response){
                is ApiResponse.Success -> {
                    _uiState.value = FoodDetailsUiState.Nothing
                    _event.emit(FoodDetailsEvent.OnAddToCard)
                }
                is ApiResponse.Error -> {
                    _uiState.value = FoodDetailsUiState.Error(response.message)
                    _event.emit(FoodDetailsEvent.ShowErrorDialog(response.message))
                }
                else -> {
                    _uiState.value = FoodDetailsUiState.Error("Exception Occurred")
                    _event.emit(FoodDetailsEvent.ShowErrorDialog("Exception Occurred"))
                }
            }
        }
    }

    fun goToCart(){
        viewModelScope.launch {
            _event.emit(FoodDetailsEvent.GoToCart)
        }
    }

    sealed class FoodDetailsUiState{
        data object  Nothing: FoodDetailsUiState()
        data object  Loading: FoodDetailsUiState()
        data object Success : FoodDetailsUiState()
        data class Error(val message: String) : FoodDetailsUiState()
    }

    sealed class FoodDetailsEvent {
        data class ShowErrorDialog(val message: String) : FoodDetailsEvent()
        data object OnAddToCard : FoodDetailsEvent()
        data object GoToCart : FoodDetailsEvent()
    }
}