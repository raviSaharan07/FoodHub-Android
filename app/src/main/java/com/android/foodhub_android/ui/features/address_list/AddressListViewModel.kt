package com.android.foodhub_android.ui.features.address_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.Address
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
class AddressListViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {
    private val _state = MutableStateFlow<AddressState>(AddressState.Loading)
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<AddressEvent?>()
    val event = _event.asSharedFlow()

    init{
       getAddress()
    }
    fun getAddress(){
        viewModelScope.launch{
            _state.value = AddressState.Loading
            val result = safeApiCall { foodApi.getUserAddress() }
            when(result){
                is ApiResponse.Success -> {
                    _state.value = AddressState.Success(result.data.addresses)
                }
                is ApiResponse.Error -> {
                    _state.value = AddressState.Error(result.message)
                }
                else ->{
                    _state.value = AddressState.Error("Something went wrong")
                }

            }
        }
    }

    fun onAddAddressClicked(){
        viewModelScope.launch {
            _event.emit(AddressEvent.NavigateToAddAddress)

        }
    }

    fun onAddressSelected(address: Address){
        viewModelScope.launch {
            _event.emit(AddressEvent.NavigateBack(address))
        }

    }

    sealed class AddressState{
        data object Loading : AddressState()
        data class Success(val data: List<Address>) : AddressState()
        data class Error(val message: String) : AddressState()
    }

    sealed class AddressEvent{
        data class NavigateToEditAddress(val address: Address) : AddressEvent()
        data object NavigateToAddAddress : AddressEvent()
        data class NavigateBack(val address: Address) : AddressEvent()
    }
}