package com.example.frontend.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.UserRepositoryImpl
import com.example.frontend.domain.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileViewModel(context: Context) : ViewModel() {

    private val repo: UserRepository =
        UserRepositoryImpl(ApiClient.userApi, AppPreferences(context))

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val user = repo.getUserInfo()
                _uiState.value = ProfileUiState(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    address = user.address ?: "",
                    phone = user.phone ?: "",
                    avatarUrl = user.avatarUrl,
                    role = user.role // 👈 gán role
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }


    fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        address: String,
        role: String,
        avatarUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            val current = _uiState.value

            fun String.toPartOrNull(): RequestBody? =
                if (this.isBlank()) null else this.toRequestBody("text/plain".toMediaTypeOrNull())

            val firstNamePart = firstName.toPartOrNull()
            val lastNamePart = lastName.toPartOrNull()
            val phonePart = phone.toPartOrNull()
            val addressPart = address.toPartOrNull()
            val rolePart = role.toPartOrNull()

            var avatarPart: MultipartBody.Part? = null
            if (avatarUri != null) {
                val inputStream = context.contentResolver.openInputStream(avatarUri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    avatarPart = MultipartBody.Part.createFormData(
                        "avatar",
                        "avatar_${System.currentTimeMillis()}.jpg",
                        requestFile
                    )
                }
            }

            try {
                val updatedUser = repo.updateProfile(
                    firstNamePart,
                    lastNamePart,
                    phonePart,
                    addressPart,
                    rolePart,
                    avatarPart
                )

                _uiState.value = _uiState.value.copy(
                    firstName = updatedUser.firstName,
                    lastName = updatedUser.lastName,
                    phone = updatedUser.phone ?: current.phone,
                    address = updatedUser.address ?: current.address,
                    role = updatedUser.role,
                    avatarUrl = updatedUser.avatarUrl ?: current.avatarUrl,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = current.copy(error = e.message)
            }
        }
    }



    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val ok = repo.logout()
                if (ok) {
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Logout failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "File not found")
                    return@launch
                }

                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData(
                    "avatar", "avatar_${System.currentTimeMillis()}.jpg", requestFile
                )

                val response = repo.updateAvatar(body)

                _uiState.value = _uiState.value.copy(
                    //avatarUrl = response.avatarUrl,
                    avatarUrl = uri.toString(),
                    isLoading = false,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

}
