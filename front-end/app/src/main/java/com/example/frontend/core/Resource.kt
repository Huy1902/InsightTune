package com.example.frontend.core

sealed class Resource<out T> {
    object Idle : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String?, val cause: Throwable? = null) : Resource<Nothing>()
}
