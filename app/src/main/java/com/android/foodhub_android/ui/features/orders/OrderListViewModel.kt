package com.android.foodhub_android.ui.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class OrderListViewModel @Inject constructor(private val foodApi: FoodApi): ViewModel() {

    private val _uiState = MutableStateFlow<OrderListState>(OrderListState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<OrderListEvent>()
    val event = _event.asSharedFlow()

    init{
        getOrders()
    }

    fun navigationToDetails(order: Order){
        viewModelScope.launch {
            _event.emit(OrderListEvent.NavigateToOrderDetailScreen(order))
        }
    }

    fun navigationBack(){
        viewModelScope.launch {
            _event.emit(OrderListEvent.NavigateBack)
        }
    }

    fun getOrders(){
        viewModelScope.launch {
            _uiState.value = OrderListState.Loading
            val result = safeApiCall {
                foodApi.getOrders()
            }
            when(result){
                is ApiResponse.Success -> {
                    _uiState.value = OrderListState.OrderList(result.data.orders)
                }
                is ApiResponse.Error -> {
                    _uiState.value = OrderListState.Error(result.message)
                }
                is ApiResponse.Exception -> {
                    _uiState.value = OrderListState.Error(result.exception.message ?: "Unknown error")
                }
            }
        }

    }

    sealed class OrderListEvent {
        data class NavigateToOrderDetailScreen(val order: Order) : OrderListEvent()
        data object NavigateBack: OrderListEvent()
    }

    sealed class OrderListState {
        data object Loading: OrderListState()
        data class OrderList(val orderList: List<Order>): OrderListState()
        data class Error(val message: String): OrderListState()
    }
}