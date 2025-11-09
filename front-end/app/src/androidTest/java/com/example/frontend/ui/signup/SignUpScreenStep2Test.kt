package com.example.frontend.ui.signup

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.frontend.R
import com.example.frontend.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpStep2ContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var passwordMismatchError: String
    private lateinit var passwordInvalidError: String
    private lateinit var backButtonDescription: String

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        passwordMismatchError = context.getString(R.string.password_do_not_match)
        passwordInvalidError = context.getString(R.string.register_password_details)
        backButtonDescription = "Back"
    }

    /**
     * KỊCH BẢN (STT 5): Lỗi - Mật khẩu không khớp
     */
    @Test
    fun signUpStep2Content_PasswordMismatch_ShowsErrorText() {
        composeTestRule.setContent {
            AppTheme {
                SignUpStep2Content(
                    password = "Password123",
                    onPasswordChange = {},
                    confirmPassword = "Password456",
                    onConfirmPasswordChange = {},
                    isPasswordVisible = false,
                    onPasswordVisibilityChange = {},
                    isConfirmVisible = false,
                    onConfirmVisibilityChange = {},
                    errorMessage = passwordMismatchError,
                    onBackClick = {},
                    onNextClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("error_message_text").assertIsDisplayed()
    }

    /**
     * KỊCH BẢN (STT 4): Lỗi - Mật khẩu không hợp lệ
     */
    @Test
    fun signUpStep2Content_InvalidPassword_ShowsErrorText() {
        composeTestRule.setContent {
            AppTheme {
                SignUpStep2Content(
                    password = "pass",
                    onPasswordChange = {},
                    confirmPassword = "pass",
                    onConfirmPasswordChange = {},
                    isPasswordVisible = false,
                    onPasswordVisibilityChange = {},
                    isConfirmVisible = false,
                    onConfirmVisibilityChange = {},
                    errorMessage = passwordInvalidError,
                    onBackClick = {},
                    onNextClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("error_message_text").assertIsDisplayed()
    }

    /**
     * KỊCH BẢN (Click): Kiểm tra click nút "Next"
     */
    @Test
    fun signUpStep2Content_ClickNext_InvokesCallback() {
        var nextClicked = false
        composeTestRule.setContent {
            AppTheme {
                SignUpStep2Content(
                    password = "Password123",
                    onPasswordChange = {},
                    confirmPassword = "Password123",
                    onConfirmPasswordChange = {},
                    isPasswordVisible = false,
                    onPasswordVisibilityChange = {},
                    isConfirmVisible = false,
                    onConfirmVisibilityChange = {},
                    errorMessage = null,
                    onBackClick = {},
                    onNextClick = { nextClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("next_button_step2").performClick()

        assertTrue("onNextClick lambda was not called", nextClicked)
    }

    /**
     * KỊCH BẢN (STT 7): Kiểm tra click nút "Back"
     */
    @Test
    fun signUpStep2Content_ClickBack_InvokesCallback() {
        var backClicked = false
        composeTestRule.setContent {
            AppTheme {
                SignUpStep2Content(
                    password = "",
                    onPasswordChange = {},
                    confirmPassword = "",
                    onConfirmPasswordChange = {},
                    isPasswordVisible = false,
                    onPasswordVisibilityChange = {},
                    isConfirmVisible = false,
                    onConfirmVisibilityChange = {},
                    errorMessage = null,
                    onBackClick = { backClicked = true },
                    onNextClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(backButtonDescription).performClick()

        assertTrue("onBackClick lambda was not called", backClicked)
    }
}