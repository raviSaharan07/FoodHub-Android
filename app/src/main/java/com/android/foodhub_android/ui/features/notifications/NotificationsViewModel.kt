package com.android.foodhub_android.ui.features.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.Notification
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
class NotificationsViewModel @Inject constructor(private val foodApi: FoodApi) : ViewModel(){

    private val _uiState = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val state = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<NotificationsEvent>()
    val event = _event.asSharedFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    init {
        getNotifications()
    }

    fun navigateToOrderDetails(orderID: String){
        viewModelScope.launch {
            _event.emit(NotificationsEvent.NavigateToOrderDetails(orderID))

        }
    }

    fun readNotification(notification: Notification){
        viewModelScope.launch{
            navigateToOrderDetails(notification.orderId)
            val response = safeApiCall { foodApi.readNotification(notification.id) }
            if(response is ApiResponse.Success){
                getNotifications()
            }
        }
    }

    fun getNotifications() {
        viewModelScope.launch {
            val response = safeApiCall { foodApi.getNotifications() }
            if (response is ApiResponse.Success) {
                _unreadCount.value = response.data.unreadCount
                _uiState.value = NotificationsState.Success(response.data.notifications)
            } else if (response is ApiResponse.Error) {
                _uiState.value = NotificationsState.Error(response.message)
            }
        }
    }

    sealed class NotificationsEvent{
        data class NavigateToOrderDetails(val orderID: String): NotificationsEvent()
    }

    sealed class NotificationsState {
        data object Loading : NotificationsState()
        data class Success(val notifications: List<Notification>) : NotificationsState()
        data class Error(val message: String) : NotificationsState()

    }
}