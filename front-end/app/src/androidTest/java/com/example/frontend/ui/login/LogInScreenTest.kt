package com.example.frontend.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.frontend.R
import com.example.frontend.core.Resource
import com.example.frontend.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogInScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * KỊCH BẢN (STT 1): Click nút "Forgot Password?"
     */
    @Test
    fun logInScreenContent_ClickForgotPassword_InvokesCallback() {
        // 1. ARRANGE
        var forgotPasswordClicked = false

        composeTestRule.setContent {
            AppTheme {
                LogInScreenContent(
                    emailValue = "",
                    onEmailChange = {},
                    passwordValue = "",
                    onPasswordChange = {},
                    uiState = Resource.Idle,
                    onLoginClick = {},
                    onBackClick = {},
                    onForgotPassword = { forgotPasswordClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("forgot_password_button").performClick()
        assertTrue("onForgotPassword lambda was not called", forgotPasswordClicked)
    }

    /**
     * KỊCH BẢN (STT 9): Click nút "Back"
     */
    @Test
    fun logInScreenContent_ClickBack_InvokesCallback() {
        // 1. ARRANGE
        var backClicked = false

        composeTestRule.setContent {
            AppTheme {
                LogInScreenContent(
                    emailValue = "",
                    onEmailChange = {},
                    passwordValue = "",
                    onPasswordChange = {},
                    uiState = Resource.Idle,
                    onLoginClick = {},
                    onBackClick = { backClicked = true }, // Đặt cờ
                    onForgotPassword = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue("onBackClick lambda was not called", backClicked)
    }
}