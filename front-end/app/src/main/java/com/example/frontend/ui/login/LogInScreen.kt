package com.example.frontend.ui.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.core.Resource
import com.example.frontend.ui.common.AppTextField
import com.example.frontend.ui.signup.AuthViewModel
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.R

@Composable
fun LogInScreen(
    vm: AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val uiState by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.resetState()
    }

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is Resource.Success) {
            Toast.makeText(context, context.getString(R.string.log_in_successfully), Toast.LENGTH_SHORT).show()
            onNext()
        }
    }

    LogInScreenContent(
        emailValue = vm.email,
        onEmailChange = vm::onEmailChange,
        passwordValue = vm.password,
        onPasswordChange = vm::onPasswordChange,
        uiState = uiState,
        onLoginClick = { vm.login() },
        onBackClick = onBack,
        onForgotPassword = onForgotPassword
    )
}

@Composable
fun LogInScreenContent(
    emailValue: String,
    onEmailChange: (String) -> Unit,
    passwordValue: String,
    onPasswordChange: (String) -> Unit,
    uiState: Resource<*>,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    onForgotPassword: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing().M)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(AppTheme.iconSize().Medium)
                    .clickable { onBackClick() },
                tint = MaterialTheme.colorScheme.onBackground
            )

            Text(
                stringResource(R.string.Log_in),
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.size(AppTheme.spacing().L))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                stringResource(R.string.login_email),
                style = AppTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing().S))

            AppTextField(
                value = emailValue,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholderText = ""
            )

            Spacer(modifier = Modifier.size(AppTheme.spacing().M))

            Text(
                stringResource(R.string.login_password),
                style = AppTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing().S))

            AppTextField(
                value = passwordValue,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholderText = "",
                isPassword = true
            )
        }

        Spacer(modifier = Modifier.size(AppTheme.spacing().XS))
        TextButton(
            onClick = {
                onForgotPassword()
            },
        ) {
            Text(
                stringResource(R.string.forgot_password),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.size(AppTheme.spacing().M))

        Button(
            onClick = onLoginClick,
            enabled = uiState !is Resource.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.color().Primary
            ),
            modifier = Modifier
                .height(56.dp)
        ) {
            if (uiState is Resource.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(AppTheme.spacing().S))
                Text("Logging in...", color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    stringResource(R.string.Log_in),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        if (uiState is Resource.Error) {
            Spacer(modifier = Modifier.height(AppTheme.spacing().M))
            Text(
                text = stringResource(R.string.log_in_error),
                style = AppTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.color().Error
            )
        }
    }
}


@Preview(name = "Default State", showSystemUi = true)
@Composable
fun LogInPreview_Default() {
    AppTheme(darkTheme = false) {
        LogInScreenContent(
            emailValue = "test@email.com",
            onEmailChange = {},
            passwordValue = "password123",
            onPasswordChange = {},
            uiState = Resource.Idle,
            onLoginClick = {},
            onBackClick = {},
            onForgotPassword = {}
        )
    }
}

@Preview(name = "Loading State", showSystemUi = true)
@Composable
fun LogInPreview_Loading() {
    AppTheme(darkTheme = true) {
        LogInScreenContent(
            emailValue = "test@email.com",
            onEmailChange = {},
            passwordValue = "password123",
            onPasswordChange = {},
            uiState = Resource.Loading,
            onLoginClick = {},
            onBackClick = {},
            onForgotPassword = {}
        )
    }
}

@Preview(name = "Error State", showSystemUi = true)
@Composable
fun LogInPreview_Error() {
    AppTheme(darkTheme = true) {
        LogInScreenContent(
            emailValue = "test@email.com",
            onEmailChange = {},
            passwordValue = "password123",
            onPasswordChange = {},
            uiState = Resource.Error(""),
            onLoginClick = {},
            onBackClick = {},
            onForgotPassword = {}
        )
    }
}