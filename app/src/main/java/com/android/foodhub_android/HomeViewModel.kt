package com.android.foodhub_android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    fun navigateToOrderDetail(orderID: String) {
        viewModelScope.launch{
            _event.emit(HomeEvent.NavigateToOrderDetail(orderID))
        }
    }

    sealed class HomeEvent {
        data class NavigateToOrderDetail(val orderID: String): HomeEvent()
    }
}