package com.example.signupandlogin.controller

import androidx.navigation.NavController
import com.example.signupandlogin.model.SignupModel
import com.example.signupandlogin.model.SignupValidator
import android.util.Log

class SignUpAndLogInController(
    private val navController: NavController,
    private val model: SignupModel
) {
    fun onSigUpClicked() {
        navController.navigate("signup_step1")
    }

    fun onLogInClicked() {
        navController.navigate("login")
    }

    fun onEmailEntered(email: String) {
        if (SignupValidator.isValidEmail(email)) {
            model.email = email
            navController.navigate("signup_step2")
        }
    }

    fun onPasswordEntered(password: String) {
        if (SignupValidator.isValidPassword(password)) {
            model.password = password
            navController.navigate("signup_step3")
        }
    }

    fun onNameEntered(name: String) {
        model.name = name
        Log.d("Signup", "Created account: $model")
    }

    fun goBack() {
        navController.popBackStack()
    }
}
