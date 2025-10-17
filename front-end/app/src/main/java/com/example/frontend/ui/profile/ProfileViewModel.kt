package com.example.frontend.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.UserRepositoryImpl
import com.example.frontend.domain.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(context: Context) : ViewModel() {

    private val repo: UserRepository =
        UserRepositoryImpl(ApiClient.authApi, ApiClient.userApi, AppPreferences(context))

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        Log.d("PROFILE_VM", "Bắt đầu gọi API getUserInfo...")
        viewModelScope.launch {
            try {
                val user = repo.getUserInfo()
                _uiState.value = ProfileUiState(
                    fullName = user.firstName + " " + user.lastName,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    address = user.address ?: "",
                    phone = user.phone ?: "",
                    avatarUrl = user.avatar,
                    role = user.role
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }


    fun updateProfile(
        firstname: String,
        lastname: String,
        address: String,
        phone: String,
        role: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val updatedUser = repo.updateProfile(firstname, lastname, address, phone, role)

                _uiState.value = _uiState.value.copy(
                    fullName = updatedUser.firstName + " " + updatedUser.lastName,
                    firstName = updatedUser.firstName,
                    lastName = updatedUser.lastName,
                    phone = updatedUser.phone,
                    address = updatedUser.address,
                    role = updatedUser.role,
                    //avatarUrl = updatedUser.avatar,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun logout(refreshToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = repo.logout(refreshToken)
                Log.d("LOGOUT_VM", "Logout response: code=${res.code}, message=${res.message}")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Unknown error")
            }
        }
    }


    fun updateAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = repo.updateAvatar(context, uri)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    avatarUrl = user.result,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }


    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            try {
                val result = repo.changePassword(oldPass, newPass)
                //  emit successMessage.value = "Password updated!"
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun getRefreshToken(): String? {
        return repo.getRefreshToken()
    }

    fun clearLocalTokens() {
        repo.clearToken()
    }
}
