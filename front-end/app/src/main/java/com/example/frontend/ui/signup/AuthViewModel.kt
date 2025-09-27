package com.example.frontend.ui.signup

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.AppPreferences
import com.example.frontend.core.Resource
import com.example.frontend.data.models.user.AuthResponseDto
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

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set

    fun onEmailChange(v: String) { email = v }
    fun onPasswordChange(v: String) { password = v }
    fun onUsernameChange(v: String) { username = v }

    fun login() {
        viewModelScope.launch {
            loginUser(email, password).collect { _state.value = it }
        }
    }

    fun register() {
        viewModelScope.launch {
            registerUser(username, email, password).collect { _state.value = it }
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
}
