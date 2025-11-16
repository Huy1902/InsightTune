package com.example.frontend.ui.signup

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.AppPreferences
import com.example.frontend.core.Resource
import com.example.frontend.data.register.AuthResponseDto
import com.example.frontend.data.register.ForgotPasswordResponse
import com.example.frontend.data.register.RegisterResponseDto
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.UserRepositoryImpl
import com.example.frontend.domain.usecases.LoginUser
import com.example.frontend.domain.usecases.RegisterUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {

    private val repo =
        UserRepositoryImpl(ApiClient.authApi, ApiClient.userApi, AppPreferences(context))
    private val loginUser = LoginUser(repo)
    private val registerUser = RegisterUser(repo)

    private val _state = MutableStateFlow<Resource<AuthResponseDto>>(Resource.Idle)
    val state: StateFlow<Resource<AuthResponseDto>> = _state

    private val _registerState = MutableStateFlow<Resource<RegisterResponseDto>>(Resource.Idle)
    val registerState: StateFlow<Resource<RegisterResponseDto>> = _registerState

    private val _forgotPasswordState = MutableStateFlow<Resource<ForgotPasswordResponse>>(Resource.Idle)

    val forgotPasswordState: StateFlow<Resource<ForgotPasswordResponse>> = _forgotPasswordState


    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var newPassword by mutableStateOf("")
        private set

    var confirmNewPassword by mutableStateOf("")
        private set


    fun onEmailChange(v: String) {
        email = v.trim()
    }

    fun onPasswordChange(v: String) {
        password = v
    }

    fun onFirstNameChange(v: String) {
        firstName = v
    }

    fun onLastNameChange(v: String) {
        lastName = v
    }

    fun onConfirmPasswordChange(v: String) {
        confirmPassword = v
    }

    fun onOtpChange(v: String) {
        otp = v
    }

    fun onNewPasswordChange(v: String) {
        newPassword = v
    }

    fun onConfirmNewPasswordChange(v: String) {
        confirmNewPassword = v
    }


    fun login(onSuccess: () -> Unit = {}) {
        Log.d("LOGIN", "email=$email, pass=$password")
        viewModelScope.launch {
            loginUser(email, password).collect { res ->
                _state.value = res
                if (res is Resource.Success) {
                    val body = res.data
                    if (body != null && body.code == 0) {
                        repo.getToken()?.let { Log.d("LOGIN", "Token saved: $it") }
                        onSuccess()
                    }
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            registerUser(firstName, lastName, email, password, confirmPassword).collect { res ->
                _registerState.value = res
                if (res is Resource.Success) {
                    if (res.data?.code == 200) {
                        onSuccess()
                    }
                }
            }
        }
    }

    fun resetState() {
        _state.value = Resource.Idle
        email = ""
        password = ""
    }

    fun resetRegisterState() {
        _registerState.value = Resource.Idle
        firstName = ""
        lastName = ""
    }

    fun checkEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val exists = repo.checkEmail(email)
                onResult(exists)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return repo.getToken() != null
    }


    fun requestOtp(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repo.requestOtp(email)
                Log.d("OTP", "Request OTP response: ${response?.code}")
                onResult(response?.code == 200)
            } catch (e: Exception) {
                Log.d("OTP", "Request OTP error: ${e.message}")
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun createNewPassword(
    ) {
        viewModelScope.launch {
            _forgotPasswordState.value = Resource.Loading
            try {
                val response = repo.forgetPassword(otp, email, newPassword, confirmNewPassword)
                Log.d("CREATE_NEW_PASSWORD", "Create new password response: ${response.code}")
                if (response.code == 200) {
                    _forgotPasswordState.value = Resource.Success(response)
                } else {
                    _forgotPasswordState.value = Resource.Error(response.message)
                }
            } catch (e: Exception) {
                Log.d("CREATE_NEW_PASSWORD", "Create new password error: ${e.message}")
                e.printStackTrace()
                _forgotPasswordState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = Resource.Idle
    }

    fun clearToken() {
        repo.clearToken()
    }
}
