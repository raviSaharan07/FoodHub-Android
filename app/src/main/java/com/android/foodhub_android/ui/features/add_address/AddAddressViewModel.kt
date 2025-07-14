package com.android.foodhub_android.ui.features.add_address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.Address
import com.android.foodhub_android.data.models.ReverseGeoCodeRequest
import com.android.foodhub_android.data.remote.ApiResponse
import com.android.foodhub_android.data.remote.safeApiCall
import com.android.foodhub_android.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAddressViewModel @Inject constructor(
    val foodApi: FoodApi,
    private val locationManager: LocationManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddAddressState>(AddAddressState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<AddAddressEvent>()
    val event = _event.asSharedFlow()

    private val _address = MutableStateFlow<Address?>(null)
    val address = _address.asStateFlow()

    fun getLocation() = locationManager.getLocation()

    fun reverseGeocode(latitude: Double, longitude: Double) {
        _uiState.value = AddAddressState.Loading
        viewModelScope.launch {
            _address.value = null
            val res = safeApiCall {
                foodApi.reverseGeocode(
                    ReverseGeoCodeRequest(
                        latitude, longitude
                    )
                )
            }
            when(res){
                is ApiResponse.Success -> {
                    _address.value = res.data
                }
                else ->{
                    _address.value = null
                    _uiState.value = AddAddressState.Error("Failed to fetch Address")
                }
            }
        }
    }

    fun onAddAddressClicked(){
        viewModelScope.launch {
            _uiState.value = AddAddressState.AddressStoring
            val result = safeApiCall {
                foodApi.storeAddress(address.value!!)
            }
            when(result) {
                is ApiResponse.Success -> {
                    _uiState.value = AddAddressState.Success
                    _event.emit(AddAddressEvent.NavigateToAddressList)
                }
                else -> {
                    _uiState.value = AddAddressState.Error("Failed to store address")
                }
            }
        }
    }

    sealed class AddAddressEvent {
        data object NavigateToAddressList : AddAddressEvent()
    }

    sealed class AddAddressState {
        data object Loading : AddAddressState()
        data object Success : AddAddressState()
        data object AddressStoring: AddAddressState()
        data class Error(val message: String) : AddAddressState()

    }
}