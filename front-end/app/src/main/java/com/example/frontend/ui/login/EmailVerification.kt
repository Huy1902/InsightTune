package com.example.frontend.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.signup.AuthViewModel
import com.example.frontend.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(
    vm: AuthViewModel,
    email: String,
    onVerify: (String) -> Unit,
    onBack: () -> Unit
) {
    var otpValue by remember { mutableStateOf(List(6) { "" }) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timer by remember { mutableStateOf(60) }
    val canResend = timer == 0

    LaunchedEffect(key1 = timer) {
        if (timer > 0) {
            delay(1000L)
            timer--
        }
    }

    OtpScreenContent(
        email = email,
        otpValue = otpValue,
        onOtpChange = { index, value ->
            val newList = otpValue.toMutableList()
            if (value.length <= 1) {
                newList[index] = value
                otpValue = newList
            }
        },
        timer = timer,
        canResend = canResend,
        onResendClick = {
            vm.requestOtp(vm.email, onResult = {})
            timer = 60
        },
        isLoading = isLoading,
        errorMessage = errorMessage,
        onVerifyClick = {
            val code = otpValue.joinToString("")
            if (code.length == 6) {
                vm.onOtpChange(code)
                isLoading = true
                onVerify(code)
            }
        },
        onBackClick = onBack
    )
}

@Composable
fun OtpScreenContent(
    email: String,
    otpValue: List<String>,
    onOtpChange: (Int, String) -> Unit,
    timer: Int,
    canResend: Boolean,
    onResendClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onVerifyClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing().M)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackClick() },
                tint = MaterialTheme.colorScheme.onBackground
            )
            Text(
                stringResource(R.string.email_verification),
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing().XL))
        Text(
            stringResource(R.string.enter_otp),
            style = AppTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing().M))
        Text(
            stringResource(R.string.otp_title),
            style = AppTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            email,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing().L))
        OtpInputRow(otpValue = otpValue, onOtpChange = onOtpChange)
        Spacer(modifier = Modifier.height(AppTheme.spacing().M))
        ResendCodeText(timer = timer, canResend = canResend, onClick = onResendClick)
        Spacer(modifier = Modifier.size(30.dp))
        Button(
            onClick = onVerifyClick,
            enabled = !isLoading && otpValue.joinToString("").length == 6,
            modifier = Modifier
                .height(56.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    stringResource(R.string.verify),
                    style = AppTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = AppTheme.typography.labelLarge,
                modifier = Modifier.padding(top = AppTheme.spacing().S)
            )
        }
    }
}

@Composable
private fun OtpInputRow(
    otpValue: List<String>,
    onOtpChange: (Int, String) -> Unit,
    otpCount: Int = 6
) {
    val focusRequesters = remember { (0 until otpCount).map { FocusRequester() } }

    BasicTextField(
        value = otpValue.joinToString(""),
        onValueChange = { newValue ->
            if (newValue.length <= otpCount && newValue.all { it.isDigit() }) {
                (0 until otpCount).forEach { index ->
                    val char = newValue.getOrNull(index)?.toString() ?: ""
                    onOtpChange(index, char)
                }
                val nextFocusIndex = newValue.length.coerceIn(0, otpCount - 1)
                focusRequesters[nextFocusIndex].requestFocus()
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                otpValue.forEachIndexed { index, value ->
                    OtpCell(
                        text = value,
                        modifier = Modifier.focusRequester(focusRequesters[index])
                    )
                    if (index != otpCount - 1) {
                        Spacer(modifier = Modifier.width(AppTheme.spacing().S))
                    }
                }
            }
        }
    )
}

@Composable
private fun OtpCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 56.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResendCodeText(timer: Int, canResend: Boolean, onClick: () -> Unit) {

    val annotatedString = buildAnnotatedString {
        append(stringResource(R.string.did_not_receive_code))
        if (canResend) {
            pushStringAnnotation(tag = "resend", annotation = "resend")
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(stringResource(R.string.resend))
            }
            pop()
        } else {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.resend_in_seconds, timer))
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                if (canResend) {
                    annotatedString.getStringAnnotations(
                        tag = "resend",
                        start = offset,
                        end = offset
                    )
                        .firstOrNull()?.let { onClick() }
                }
            },
            style = AppTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )
    }
}


@Preview(name = "Light Mode", showSystemUi = true)
@Preview(name = "Dark Mode", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OtpScreenPreview() {
    AppTheme {
        OtpScreenContent(
            email = "preview@email.com",
            otpValue = listOf("1", "2", "3", "", "", ""),
            onOtpChange = { _, _ -> },
            timer = 30,
            canResend = false,
            onResendClick = {},
            isLoading = false,
            errorMessage = "Mã không hợp lệ.",
            onVerifyClick = {},
            onBackClick = {}
        )
    }
}