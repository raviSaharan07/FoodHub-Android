package com.android.foodhub_android.ui.features.auth

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.auth.GoogleAuthUiProvider
import com.android.foodhub_android.data.models.OAuthRequest
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel(open val foodApi: FoodApi) : ViewModel() {

    private val googleAuthUiProvider = GoogleAuthUiProvider()
    private lateinit var callbackManager: CallbackManager

    abstract fun loading()
    abstract fun onGoogleError(msg: String)
    abstract fun onFacebookError(msg: String)
    abstract fun onSocialLoginInSuccess(token:String)

    fun onFacebookClicked(context: ComponentActivity){
        initiateFacebookLogin(context)
    }

    fun onGoogleSignInClicked(context: ComponentActivity){
        initiateGoogleLogin(context)
    }

    protected fun initiateGoogleLogin(context: ComponentActivity){
        viewModelScope.launch {
            loading()
            val response = googleAuthUiProvider.signIn(
                context,
                CredentialManager.create(context)
            )

            if(response != null){
                val request = OAuthRequest(
                    token = response.token,
                    provider = "google"
                )
                val res = foodApi.oAuth(request)
                if(res.token.isNotEmpty()){
                    Log.d("SignInViewModel", "onGoogleSignInClicked: ${res.token}")
                    onSocialLoginInSuccess(res.token)
                }else {
                    onGoogleError("Failed")
                }
            }else{
                onGoogleError("Failed")
            }
        }
    }

    protected fun initiateFacebookLogin(context: ComponentActivity){
        loading()
        callbackManager = create()
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object: FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModelScope.launch {
                        val request = OAuthRequest(
                            token = result.accessToken.token,
                            provider = "facebook"
                        )
                        val res = foodApi.oAuth(request)
                        if (res.token.isNotEmpty()) {
                            Log.d("SignInViewModel", "onFacebookSignInClicked: ${res.token}")
                            onSocialLoginInSuccess(res.token)
                        } else {
                            onFacebookError("Failed no Token")
                        }
                    }
                }

                override fun onCancel() {
                    onFacebookError("Cancelled")
                }

                override fun onError(error: FacebookException) {
                    onFacebookError(error.message.toString())
                }
            })

        LoginManager.getInstance().logInWithReadPermissions(
            context,
            callbackManager,
            listOf("public_profile","email")
        )
    }
}