package com.android.foodhub_android.ui.features.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.Address
import com.android.foodhub_android.data.models.CartItem
import com.android.foodhub_android.data.models.CartResponse
import com.android.foodhub_android.data.models.ConfirmPaymentRequest
import com.android.foodhub_android.data.models.PaymentIntentRequest
import com.android.foodhub_android.data.models.PaymentIntentResponse
import com.android.foodhub_android.data.models.UpdateCartItemRequest
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
class CartViewModel @Inject constructor(val foodApi: FoodApi) : ViewModel() {
    var errorTitle: String = ""
    var errorMessage: String = ""

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<CartEvent>()
    val event = _event.asSharedFlow()

    private var cartResponse: CartResponse? = null
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount = _cartItemCount.asStateFlow()
    private var paymentIntent: PaymentIntentResponse? = null

    private val address = MutableStateFlow<Address?>(null)
    val selectedAddress = address.asStateFlow()

    init{
        getCart()
    }

    fun getCart(){
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val res = safeApiCall { foodApi.getCart() }
            when(res){
                is ApiResponse.Success -> {
                    Log.d("CartViewModel", "Cart response: ${res.data}")
                    cartResponse = res.data
                    _cartItemCount.value = res.data.items.size
                    _uiState.value = CartUiState.Success(res.data)
                }
                is ApiResponse.Error -> {
                    Log.d("CartViewModel", "Error response: ${res.message}")
                    _uiState.value = CartUiState.Error(res.message)

                }
                else -> {
                    Log.d("CartViewModel", "Something went wrong")
                    _uiState.value = CartUiState.Error("Something went wrong")
                }
            }

        }
    }

    fun incrementQuantity(cartItem: CartItem){
        if(cartItem.quantity == 5){
            return
        }
        updateItemQuantity(cartItem,cartItem.quantity + 1)
    }

    fun decrementQuantity(cartItem: CartItem){
        if(cartItem.quantity == 1){
            return
        }
        updateItemQuantity(cartItem,cartItem.quantity - 1)
    }

    private fun updateItemQuantity(cartItem: CartItem, quantity: Int){
        viewModelScope.launch{
            _uiState.value = CartUiState.Loading
            val res = safeApiCall { foodApi.updateCart(
                UpdateCartItemRequest(
                    cartItem.id,
                    quantity
                )
            ) }
            when(res){
                is ApiResponse.Success -> {
                    getCart()
                }
                else -> {
                    cartResponse?.let {
                        _uiState.value = CartUiState.Success(it)
                    }
                    errorTitle = "Cannot Update Quantity"
                    errorMessage = "Something went wrong while updating quantity"
                    _event.emit(CartEvent.OnQuantityUpdateError)
                }
            }
        }
    }

    fun removeItem(cartItem: CartItem){
        viewModelScope.launch{
            _uiState.value = CartUiState.Loading
            val res = safeApiCall { foodApi.deleteCartItem(cartItem.id) }
            when(res){
                is ApiResponse.Success -> {
                    getCart()
                }
                else -> {
                    cartResponse?.let {
                        _uiState.value = CartUiState.Success(it)
                    }
                    errorTitle = "Cannot Remove Item"
                    errorMessage = "Something went wrong while removing item"
                    _event.emit(CartEvent.OnItemRemovedError)
                }
            }
        }
    }

    fun checkout(){
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val paymentDetails = safeApiCall {
                foodApi.getPaymentIntent(
                    PaymentIntentRequest(
                        address.value!!.id!!
                    )
                )
            }
            when(paymentDetails){
                is ApiResponse.Success -> {
                    paymentIntent = paymentDetails.data
                    _event.emit(CartEvent.OnInitiatePayment(paymentDetails.data))
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }
                else -> {
                    errorTitle = "Cannot Checkout"
                    errorMessage = "Something went wrong while checking out"
                    _event.emit(CartEvent.ShowErrorDialog)
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }
            }
        }

    }

    fun onAddressClicked(){
        viewModelScope.launch{
            _event.emit(CartEvent.OnAddressClicked)
        }
    }

    fun onAddressSelected(address: Address){
        this.address.value = address
    }

    fun onPaymentFailed(){
        errorTitle = "Payment Failed"
        errorMessage = "Something went wrong while processing payment"
        viewModelScope.launch {
            _event.emit(CartEvent.ShowErrorDialog)
        }
    }

    fun onPaymentSuccess(){
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            val response = safeApiCall {
                foodApi.verifyPurchase(
                    ConfirmPaymentRequest(
                        paymentIntent!!.paymentIntentId,
                        address.value!!.id!!
                    ),paymentIntent!!.paymentIntentId
                )
            }
            when(response){
                is ApiResponse.Success -> {
                    _event.emit(CartEvent.OrderSuccess(response.data.orderId))
                    _uiState.value = CartUiState.Success(cartResponse!!)
                    getCart()
                }
                else -> {
                    errorTitle = "Payment Failed"
                    errorMessage = "Something went wrong while processing payment"
                    _event.emit(CartEvent.ShowErrorDialog)
                    _uiState.value = CartUiState.Success(cartResponse!!)
                }
            }
        }

    }

    sealed class CartUiState{
        data object  Nothing: CartUiState()
        data object  Loading: CartUiState()
        data class Success(val data: CartResponse) : CartUiState()
        data class Error(val message: String) : CartUiState()
    }

    sealed class CartEvent {
        data object ShowErrorDialog : CartEvent()
        data class OrderSuccess(val orderId: String?) : CartEvent()
        data object OnCheckout : CartEvent()
        data class OnInitiatePayment(val data: PaymentIntentResponse) : CartEvent()
        data object OnQuantityUpdateError: CartEvent()
        data object OnItemRemovedError: CartEvent()
        data object OnAddressClicked: CartEvent()
    }

}