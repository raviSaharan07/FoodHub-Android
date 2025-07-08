package com.android.foodhub_android.ui.features.auth.login

import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.FoodHubSession
import com.android.foodhub_android.data.models.SignInRequest
import com.android.foodhub_android.data.remote.ApiResponse
import com.android.foodhub_android.data.remote.safeApiCall
import com.android.foodhub_android.ui.features.auth.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@HiltViewModel
class SignInViewModel @Inject constructor(override val foodApi : FoodApi, val session: FoodHubSession) : BaseAuthViewModel(foodApi) {
    private val _uiState = MutableStateFlow<SignInEvent>(SignInEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SignInNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun onEmailChange(email: String){
        _email.value = email
    }

    fun onPasswordChange(password: String){
        _password.value = password
    }

    fun onSignInClick(){
        viewModelScope.launch {
            _uiState.value = SignInEvent.Loading
                val response = safeApiCall {
                    foodApi.signIn(SignInRequest(
                        email = email.value,
                        password = password.value
                    ))
                }
            when(response){
                is ApiResponse.Success -> {
                    _uiState.value = SignInEvent.Success
                    session.storeToken(response.data.token)
                    _navigationEvent.emit(SignInNavigationEvent.NavigateToHome)
                }
                else -> {
                    val err = (response as? ApiResponse.Error)?.code ?: 500
                    error = "Sign In Failed"
                    errorDescription = "Failed to sign up"
                    when(err){
                        400 -> {
                            error = "Invalid Credentials"
                            errorDescription = "Invalid email or password"
                        }
                    }
                    _uiState.value = SignInEvent.Error
                }
            }
        }
    }
    fun onSignUpClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SignInNavigationEvent.NavigateToSignUp)
        }
    }

    sealed class SignInNavigationEvent{
        data object NavigateToSignUp : SignInNavigationEvent()
        data object NavigateToHome : SignInNavigationEvent()
        data object showErrorDialog : SignInNavigationEvent()
    }

    sealed class SignInEvent{
        data object Nothing : SignInEvent()
        data object Success : SignInEvent()
        data object Error : SignInEvent()
        data object Loading : SignInEvent()
    }

    override fun loading() {
        viewModelScope.launch {
            _uiState.value = SignInEvent.Loading
        }
    }

    override fun onGoogleError(msg: String) {
        viewModelScope.launch {
            error = "Google Sign In Failed"
            errorDescription = msg
            _uiState.value = SignInEvent.Error
            _navigationEvent.emit(SignInNavigationEvent.showErrorDialog)
        }
    }

    override fun onFacebookError(msg: String) {
        viewModelScope.launch {
            error = "Facebook Sign In Failed"
            errorDescription = msg
            _uiState.value = SignInEvent.Error
            _navigationEvent.emit(SignInNavigationEvent.showErrorDialog)
        }
    }

    override fun onSocialLoginInSuccess(token: String) {
        viewModelScope.launch {
            _uiState.value = SignInEvent.Success
            session.storeToken(token)
            _navigationEvent.emit(SignInNavigationEvent.NavigateToHome)
        }
    }
}