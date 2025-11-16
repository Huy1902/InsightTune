package com.example.frontend.ui.signup

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.frontend.core.Resource
import com.example.frontend.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpStep3ContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var backButtonDescription: String

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        backButtonDescription = "Back"
    }

    /**
     * KỊCH BẢN (Loading): Kiểm tra trạng thái đang tải
     */
    @Test
    fun signUpStep3Content_LoadingState_ShowsSpinnerAndDisablesButton() {
        composeTestRule.setContent {
            AppTheme {
                SignUpStep3Content(
                    firstName = "John",
                    onFirstNameChange = {},
                    lastName = "Doe",
                    onLastNameChange = {},
                    onBackClick = {},
                    onCreateAccountClick = {},
                    uiState = Resource.Loading
                )
            }
        }
        composeTestRule.onNodeWithTag("create_account_button").assertIsNotEnabled()
    }

    /**
     * KỊCH BẢN (STT 6): Lỗi - API đăng ký thất bại
     */
    @Test
    fun signUpStep3Content_ErrorState_ShowsErrorText() {
        val errorMessage = "Simulated API error"

        composeTestRule.setContent {
            AppTheme {
                SignUpStep3Content(
                    firstName = "John",
                    onFirstNameChange = {},
                    lastName = "Doe",
                    onLastNameChange = {},
                    onBackClick = {},
                    onCreateAccountClick = {},
                    uiState = Resource.Error(errorMessage)
                )
            }
        }
        composeTestRule.onNodeWithTag("error_message_text").assertIsDisplayed()
    }


    /**
     * KỊCH BẢN (Click): Kiểm tra click nút "Create Account"
     */
    @Test
    fun signUpStep3Content_ClickCreateAccount_InvokesCallback() {
        var createClicked = false
        composeTestRule.setContent {
            AppTheme {
                SignUpStep3Content(
                    firstName = "John",
                    onFirstNameChange = {},
                    lastName = "Doe",
                    onLastNameChange = {},
                    onBackClick = {},
                    onCreateAccountClick = { createClicked = true },
                    uiState = Resource.Idle
                )
            }
        }
        composeTestRule.onNodeWithTag("create_account_button").performClick()
        assertTrue("onCreateAccountClick lambda was not called", createClicked)
    }

    /**
     * KỊCH BẢN (STT 7): Kiểm tra click nút "Back"
     */
    @Test
    fun signUpStep3Content_ClickBack_InvokesCallback() {
        var backClicked = false
        composeTestRule.setContent {
            AppTheme {
                SignUpStep3Content(
                    firstName = "",
                    onFirstNameChange = {},
                    lastName = "",
                    onLastNameChange = {},
                    onBackClick = { backClicked = true },
                    onCreateAccountClick = {},
                    uiState = Resource.Idle
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(backButtonDescription).performClick()
        assertTrue("onBackClick lambda was not called", backClicked)
    }
}