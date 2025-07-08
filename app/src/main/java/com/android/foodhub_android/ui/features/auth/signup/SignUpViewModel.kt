package com.android.foodhub_android.ui.features.auth.signup

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.FoodHubSession
import com.android.foodhub_android.data.models.SignUpRequest
import com.android.foodhub_android.data.remote.ApiResponse
import com.android.foodhub_android.data.remote.safeApiCall
import com.android.foodhub_android.ui.features.auth.AuthScreenViewModel.AuthEvent
import com.android.foodhub_android.ui.features.auth.BaseAuthViewModel
import com.android.foodhub_android.ui.features.auth.login.SignInViewModel.SignInEvent
import com.android.foodhub_android.ui.features.auth.login.SignInViewModel.SignInNavigationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SignUpViewModel @Inject constructor(override val foodApi : FoodApi, val session: FoodHubSession) : BaseAuthViewModel(foodApi) {

    private val _uiState = MutableStateFlow<SignupEvent>(SignupEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SignupNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    fun onEmailChange(email: String){
        _email.value = email
    }

    fun onPasswordChange(password: String){
        _password.value = password
    }

    fun onNameChange(name: String){
        _name.value = name
    }

    fun onSignUpClick(){
        viewModelScope.launch {
            _uiState.value = SignupEvent.Loading
            try {
                val response = safeApiCall { foodApi.signUp(
                    SignUpRequest(
                        name = name.value,
                        email = email.value,
                        password = password.value
                    )
                ) }
                when(response){
                    is ApiResponse.Success ->{
                        _uiState.value = SignupEvent.Success
                        session.storeToken(response.data.token)
                        _navigationEvent.emit(SignupNavigationEvent.NavigateToHome)
                    }
                    else ->{
                        val err = (response as? ApiResponse.Error)?.code
                        error = "Sign In Failed"
                        errorDescription = "Failed to sign up"

                        when(err){
                            400 -> {
                                error = "Invalid Credentials"
                                errorDescription = "Please enter correct details."
                            }
                        }
                        _uiState.value = SignupEvent.Error
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
                _uiState.value = SignupEvent.Error
            }
        }


    }

    fun onLoginClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SignupNavigationEvent.NavigateToLogin)
        }
    }

    sealed class SignupNavigationEvent{
        object NavigateToLogin : SignupNavigationEvent()
        object NavigateToHome : SignupNavigationEvent()
        object showErrorDialog : SignupNavigationEvent()
    }

    sealed class SignupEvent{
        object Nothing : SignupEvent()
        object Success : SignupEvent()
        object Error : SignupEvent()
        object Loading : SignupEvent()
    }

    override fun loading() {
        viewModelScope.launch {
            _uiState.value = SignupEvent.Loading
        }
    }

    override fun onGoogleError(msg: String) {
        viewModelScope.launch {
            error = "Google Sign In Failed"
            errorDescription = msg
            _uiState.value = SignupEvent.Error
            _navigationEvent.emit(SignupNavigationEvent.showErrorDialog)
        }
    }

    override fun onFacebookError(msg: String) {
        viewModelScope.launch {
            error = "Facebook Sign In Failed"
            errorDescription = msg
            _uiState.value = SignupEvent.Error
            _navigationEvent.emit(SignupNavigationEvent.showErrorDialog)
        }
    }

    override fun onSocialLoginInSuccess(token: String) {
        viewModelScope.launch {
            session.storeToken(token)
            _uiState.value = SignupEvent.Success
            _navigationEvent.emit(SignupNavigationEvent.NavigateToHome)
        }
    }
}