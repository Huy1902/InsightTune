package com.example.frontend.ui.login

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.common.AppTextField
import com.example.frontend.ui.signup.AuthViewModel
import com.example.frontend.ui.theme.AppTheme

@Composable
fun ForgetPasswordScreen(
    vm: AuthViewModel,
    onNavigateToOtp: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ForgetPasswordScreenContent(
        email = email,
        onEmailChange = { email = it },
        isLoading = isLoading,
        onSendClick = {
            if (email.isNotBlank()) {
                isLoading = true
                vm.requestOtp(email) { success ->
                    vm.onEmailChange(email)
                    isLoading = false
                    if (success) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.email_verification_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        onNavigateToOtp(email)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.email_verification_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        },
        onBackClick = onBack
    )
}

@Composable
fun ForgetPasswordScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onSendClick: () -> Unit,
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
                stringResource(R.string.forgot_password),
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing().XL))
        Text(
            stringResource(R.string.login_email),
            style = AppTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing().S))
        Text(
            stringResource(R.string.send_otp_details),
            style = AppTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing().L))
        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            placeholderText = "Email"
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onSendClick,
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.send_otp))
            }
        }
    }
}

@Preview(name = "Light Mode", showSystemUi = true)
@Preview(name = "Dark Mode", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ForgetPasswordScreenPreview() {
    AppTheme {
        ForgetPasswordScreenContent(
            email = "preview@email.com",
            onEmailChange = {},
            isLoading = false,
            onSendClick = {},
            onBackClick = {}
        )
    }
}