package com.android.foodhub_android.ui.features.order_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.R
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.Order
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
class OrderDetailsViewModel @Inject constructor(private val foodApi: FoodApi) : ViewModel() {
    private val _uiState = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<OrderDetailsEvent>()
    val event = _event.asSharedFlow()

    fun getOrderDetails(orderID: String) {
        _uiState.value = OrderDetailsState.Loading
        viewModelScope.launch {
            val result = safeApiCall {
                foodApi.getOrderDetails(orderID)
            }
            when (result) {
                is ApiResponse.Success -> {
                    _uiState.value = OrderDetailsState.OrderDetails(result.data)
                }
                is ApiResponse.Error -> {
                    _uiState.value = OrderDetailsState.Error(result.message)
                }
                is ApiResponse.Exception -> {
                    _uiState.value = OrderDetailsState.Error(result.exception.message ?: "Unknown error")
                }
            }
        }
    }

    fun navigateBack(){
        viewModelScope.launch {
            _event.emit(OrderDetailsEvent.NavigateBack)
        }
    }

    fun getImage(order: Order): Int {
        return when(order.status){
            "Delivered" -> R.drawable.ic_delivered
            "Preparing" -> R.drawable.ic_preparing
            "On the way" -> R.drawable.picked_by_rider_icon
            else -> R.drawable.ic_pending
        }
    }

    sealed class OrderDetailsEvent {
        data object NavigateBack : OrderDetailsEvent()
    }

    sealed class OrderDetailsState {
        data object Loading : OrderDetailsState()
        data class OrderDetails(val order: Order) : OrderDetailsState()
        data class Error(val message: String) : OrderDetailsState()
    }
}