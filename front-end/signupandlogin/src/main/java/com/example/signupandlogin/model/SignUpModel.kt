package com.example.signupandlogin.model

data class SignupModel(
    var email: String = "",
    var password: String = "",
    var name: String = ""
)

object SignupValidator {
    fun isValidEmail(email: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return regex.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        val regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
        return regex.matches(password)
    }
}
