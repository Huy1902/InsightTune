package com.example.frontend.ui.signup

import android.util.Log
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.common.AppTextField
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.R
import com.example.frontend.core.Resource

@Composable
fun SignUpScreenStep3(vm: AuthViewModel, onNext: () -> Unit, onBack: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val context = LocalContext.current

    val uiState by vm.registerState.collectAsState()

    LaunchedEffect(Unit) {
        vm.resetRegisterState()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.register_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                onNext()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.register_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }
    SignUpStep3Content(
        firstName = firstName,
        onFirstNameChange = { firstName = it },
        lastName = lastName,
        onLastNameChange = { lastName = it },
        onBackClick = onBack,
        onCreateAccountClick = {
            if (firstName.isNotBlank() && lastName.isNotBlank()) {
                vm.onFirstNameChange(firstName)
                vm.onLastNameChange(lastName)
            }
            vm.onFirstNameChange(firstName)
            vm.onLastNameChange(lastName)
            Log.d("REGISTER_UI", "email=${vm.email}, pass=${vm.password}")
            vm.register {}
        },
        uiState = uiState
    )
}

@Composable
fun SignUpStep3Content(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    uiState: Resource<*>? = null
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
                stringResource(R.string.register_first_name),
                style = AppTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.size(AppTheme.spacing().M))
            AppTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firstname_field"),
                textColor = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                stringResource(R.string.register_last_name),
                style = AppTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.size(AppTheme.spacing().M))

            AppTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lastname_field"),
                textColor = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.size(30.dp))

        Button(
            onClick = onCreateAccountClick,
            enabled = uiState !is Resource.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .height(56.dp)
                .testTag("create_account_button")
        ) {
            if (uiState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    stringResource(R.string.register_confirmation),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = AppTheme.typography.titleLarge
                )
            }
        }

        if (uiState is Resource.Error) {
            Spacer(modifier = Modifier.height(AppTheme.spacing().M))
            Text(
                text = "An error occurred: ${uiState.message}",
                style = AppTheme.typography.labelLarge,
                color = AppTheme.color().Error
            )
        }
    }
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun SignUpScreenStep3Preview() {
    AppTheme {
        SignUpStep3Content(
            firstName = "John", onFirstNameChange = {},
            lastName = "Doe", onLastNameChange = {},
            onBackClick = {}, onCreateAccountClick = {}
        )
    }
}

@Preview(name = "Loading State", showSystemUi = true)
@Composable
fun SignUpScreenStep3LoadingPreview() {
    AppTheme(darkTheme = true) {
        SignUpStep3Content(
            firstName = "John", onFirstNameChange = {},
            lastName = "Doe", onLastNameChange = {},
            onBackClick = {}, onCreateAccountClick = {}
        )
    }
}