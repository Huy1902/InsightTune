package com.example.frontend.ui.signup

import android.content.Context
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
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
class SignUpStep1ContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * KỊCH BẢN (STT 3): Lỗi - Email đã tồn tại
     * * Test này kiểm tra: "Khi errorMessage CÓ dữ liệu,
     * UI có hiển thị text lỗi đó không?"
     */
    @Test
    fun signUpStep1Content_ErrorState_ShowsErrorText() {
        val emailInput = "existing@example.com"
        val errorMessage = "Email already exists. Please try another one."

        composeTestRule.setContent {
            AppTheme {
                SignUpStep1Content(
                    email = emailInput,
                    onEmailChange = {},
                    isLoading = false,
                    errorMessage = errorMessage,
                    onBackClick = {},
                    onNextClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        composeTestRule.onNodeWithTag("email_field")
            .assertTextEquals(emailInput)
    }

    /**
     * KỊCH BẢN (STT 8): Đang tải (Loading)
     * * Test này kiểm tra: "Khi isLoading = true,
     * nút Next có bị vô hiệu hóa và hiển thị chữ 'check' không?"
     */
    @Test
    fun signUpStep1Content_LoadingState_ShowsSpinnerAndDisablesButton() {
        val loadingText = context.getString(R.string.check)

        composeTestRule.setContent {
            AppTheme {
                SignUpStep1Content(
                    email = "test@example.com",
                    onEmailChange = {},
                    isLoading = true,
                    errorMessage = null,
                    onBackClick = {},
                    onNextClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("next_button_step1").assertIsNotEnabled()

        composeTestRule.onNodeWithText(loadingText).assertIsDisplayed()
    }

    /**
     * KỊCH BẢN (Click): Kiểm tra tương tác
     * * Test này kiểm tra: "Khi click vào nút Next,
     * lambda onNextClick có được gọi không?"
     */
    @Test
    fun signUpStep1Content_ClickNext_InvokesCallback() {
        var nextClicked = false

        composeTestRule.setContent {
            AppTheme {
                SignUpStep1Content(
                    email = "test@example.com",
                    onEmailChange = {},
                    isLoading = false,
                    errorMessage = null,
                    onBackClick = {},
                    onNextClick = {
                        nextClicked = true
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("next_button_step1").performClick()

        assertTrue("onNextClick lambda was not called", nextClicked)
    }
}