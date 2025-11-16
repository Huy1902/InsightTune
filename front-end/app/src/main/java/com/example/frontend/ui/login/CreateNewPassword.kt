package com.example.frontend.ui.login

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.core.Resource
import com.example.frontend.ui.common.AppTextField
import com.example.frontend.ui.signup.AuthViewModel
import com.example.frontend.ui.theme.AppTheme

fun isValidPassword(password: String): Boolean {
    val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
    return passwordRegex.matches(password)
}

@Composable
fun CreateNewPassword(vm: AuthViewModel, onNext: () -> Unit, onBack: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val uiState = vm.forgotPasswordState.collectAsState().value
    LaunchedEffect(Unit) {
        vm.resetForgotPasswordState()
    }
    LaunchedEffect(uiState) {
        if (uiState is Resource.Success) {
            Toast.makeText(
                context,
                context.getString(R.string.create_new_password_successfully),
                Toast.LENGTH_SHORT
            ).show()
            onNext()
        }
    }
    CreateNewPasswordContent(
        password = password,
        onPasswordChange = {
            password = it
            if (uiState is Resource.Error) vm.resetForgotPasswordState()
        },
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = {
            confirmPassword = it
            if (uiState is Resource.Error) vm.resetForgotPasswordState()
        },
        uiState = uiState,
        onBackClick = onBack,
        onNextClick = {
            when {
                !isValidPassword(password) -> {
                }
                password != confirmPassword -> {
                }
                else -> {
                    vm.onNewPasswordChange(password)
                    vm.onConfirmNewPasswordChange(confirmPassword)
                    vm.createNewPassword()
                }
            }
        },
        isLoading = uiState is Resource.Loading
    )
}

@Composable
fun CreateNewPasswordContent(
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    uiState: Resource<*>,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
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
                stringResource(R.string.create_new_password),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                stringResource(R.string.register_password),
                style = AppTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.size(4.dp))
            AppTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholderText = "",
                isPassword = true
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                stringResource(R.string.register_password_details),
                style = AppTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.size(15.dp))
            Text(
                stringResource(R.string.register_password_confirmation),
                style = AppTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.size(4.dp))
            AppTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholderText = "",
                isPassword = true
            )
        }

        Spacer(modifier = Modifier.size(30.dp))

        Button(
            onClick = onNextClick,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    stringResource(R.string.confirm),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        if (uiState is Resource.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                uiState.message ?: "Unknown error",
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateNewPasswordPreview() {

}