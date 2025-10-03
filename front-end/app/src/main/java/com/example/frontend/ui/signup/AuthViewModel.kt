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
import com.example.frontend.data.models.user.AuthResponseDto
import com.example.frontend.data.models.user.RegisterResponseDto
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.UserRepositoryImpl
import com.example.frontend.domain.usecases.LoginUser
import com.example.frontend.domain.usecases.RegisterUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {

    private val repo = UserRepositoryImpl(ApiClient.userApi, AppPreferences(context))
    private val loginUser = LoginUser(repo)
    private val registerUser = RegisterUser(repo)

    private val _state = MutableStateFlow<Resource<AuthResponseDto>>(Resource.Idle)
    val state: StateFlow<Resource<AuthResponseDto>> = _state

    private val _registerState = MutableStateFlow<Resource<RegisterResponseDto>>(Resource.Idle)
    val registerState: StateFlow<Resource<RegisterResponseDto>> = _registerState


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

    fun onEmailChange(v: String) {
        email = v.trim()
    }
    fun onPasswordChange(v: String) { password = v }
    fun onFirstNameChange(v: String) { firstName = v }
    fun onLastNameChange(v: String) { lastName = v }
    fun onConfirmPasswordChange(v: String) { confirmPassword = v }

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
        // nếu mày có lưu email/pass trong ViewModel cũng nên clear luôn
        email = ""
        password = ""
    }

    fun refreshAccessToken(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val refreshToken = repo.getRefreshToken()
            if (refreshToken != null) {
                try {
                    val res = repo.refreshToken(refreshToken)
                    if (res.code == 200) {
                        onSuccess()
                    } else {
                        _state.value = Resource.Error("Refresh failed: ${res.message}")
                    }
                } catch (e: Exception) {
                    _state.value = Resource.Error(e.message ?: "Unknown error")
                }
            }
        }
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

}
