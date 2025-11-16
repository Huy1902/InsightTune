package com.example.frontend.data.models.login

import com.google.gson.annotations.SerializedName

data class RefreshRequest(
    @SerializedName("token")
    val token: String
)