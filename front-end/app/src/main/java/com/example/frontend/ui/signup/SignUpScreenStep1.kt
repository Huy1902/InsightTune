package com.example.frontend.ui.signup

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.common.AppTextField
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.R

val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

fun isValidEmail(email: String): Boolean {
    return emailRegex.matches(email)
}

@Composable
fun SignUpScreenStep1(vm: AuthViewModel, onNext: () -> Unit, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    SignUpStep1Content(
        email = email,
        onEmailChange = {
            email = it
            errorMessage = null
        },
        isLoading = isLoading,
        errorMessage = errorMessage,
        onBackClick = onBack,
        onNextClick = {
            if (!isValidEmail(email)) {
                errorMessage = "Invalid email format."
            } else {
                isLoading = true
                vm.checkEmail(email) { exists ->
                    isLoading = false
                    if (exists) {
                        errorMessage = "Email already exists. Please try another one."
                    } else {
                        vm.onEmailChange(email)
                        onNext()
                    }
                }
            }
        }
    )
}

@Composable
fun SignUpStep1Content(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
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
                stringResource(R.string.register_title),
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
                stringResource(R.string.register_email),
                style = AppTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.size(AppTheme.spacing().M))

            AppTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholderText = ""
            )

            Spacer(modifier = Modifier.size(4.dp))

//            Text(
//                stringResource(R.string.register_email_details),
//                fontWeight = FontWeight.Bold,
//                style = AppTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier.align(Alignment.Start)
//            )
        }

        Spacer(modifier = Modifier.size(30.dp))

        Button(
            onClick = onNextClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .height(56.dp),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.check),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.labelSmall,
                    fontSize = 14.sp,
                )
            } else {
                Text(
                    stringResource(R.string.next),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                it,
                style = AppTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true, name = "Normal State")
@Composable
fun SignUpScreenStep1Preview() {
    AppTheme {
        SignUpStep1Content(
            email = "hello@world.com",
            onEmailChange = {},
            isLoading = false,
            errorMessage = null,
            onBackClick = {},
            onNextClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading State")
@Composable
fun SignUpScreenStep1LoadingPreview() {
    AppTheme {
        SignUpStep1Content(
            email = "hello@world.com",
            onEmailChange = {},
            isLoading = true,
            errorMessage = null,
            onBackClick = {},
            onNextClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error State")
@Composable
fun SignUpScreenStep1ErrorPreview() {
    AppTheme {
        SignUpStep1Content(
            email = "hello@world.com",
            onEmailChange = {},
            isLoading = false,
            errorMessage = "Email already exists. Please try another one.",
            onBackClick = {},
            onNextClick = {}
        )
    }
}