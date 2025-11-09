package com.example.frontend.ui.start

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class StartScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var slogan1: String
    private lateinit var signUp: String
    private lateinit var google: String
    private lateinit var facebook: String
    private lateinit var logIn: String
    private lateinit var appLogoDescription: String

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        slogan1 = context.getString(R.string.slogan_1)
        signUp = context.getString(R.string.sign_up)
        google = context.getString(R.string.Google)
        facebook = context.getString(R.string.Facebook)
        logIn = context.getString(R.string.Log_in)
        appLogoDescription = "App logo"
    }

    /**
     * Kịch bản 1: Kiểm tra xem tất cả các thành phần
     * quan trọng có hiển thị khi màn hình được load không.
     */
    @Test
    fun startScreen_AllElementsDisplayed() {
        composeTestRule.setContent {
            AppTheme {
                StartScreenContent(
                    onSignUpClick = {},
                    onLoginClick = {},
                    onGoogleClick = {},
                    onFacebookClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(appLogoDescription)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(slogan1).assertIsDisplayed()
        composeTestRule.onNodeWithText(signUp).assertIsDisplayed()
        composeTestRule.onNodeWithText(google).assertIsDisplayed()
        composeTestRule.onNodeWithText(facebook).assertIsDisplayed()
        composeTestRule.onNodeWithText(logIn).assertIsDisplayed()
    }

    /**
     * Kịch bản 2: Kiểm tra tương tác
     * Khi click nút "Sign up", xác nhận lambda onSignUpClick được gọi.
     */
    @Test
    fun startScreen_ClickSignUp_InvokesCallback() {
        var signUpClicked = false

        composeTestRule.setContent {
            AppTheme {
                StartScreenContent(
                    onSignUpClick = { signUpClicked = true },
                    onLoginClick = {},
                    onGoogleClick = {},
                    onFacebookClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(signUp).performClick()
        assertTrue("onSignUpClick lambda was not called", signUpClicked)
    }
}