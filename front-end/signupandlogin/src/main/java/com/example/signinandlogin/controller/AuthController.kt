package com.example.signinandlogin.controller

import com.example.signinandlogin.model.AuthResult
import com.example.signinandlogin.model.User

class AuthController {
    var result: AuthResult? = null
        private set

    fun login(email: String, password: String, updateUI: () -> Unit) {
        result = if (email == "test@gmail.com" && password == "123456") {
            AuthResult(true, "Login successful")
        } else {
            AuthResult(false, "Wrong email or password")
        }
        updateUI()
    }

    fun signUp(email: String, password: String, updateUI: () -> Unit) {
        result = AuthResult(true, "Sign up successful for $email")
        updateUI()
    }
}