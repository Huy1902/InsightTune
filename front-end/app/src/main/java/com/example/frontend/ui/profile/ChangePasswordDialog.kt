package com.example.frontend.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.ui.common.AppTextField
import com.example.frontend.ui.theme.AppTheme

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPass: String, newPass: String) -> Unit,
    uiState: ProfileUiState
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }
    val context = LocalContext.current



    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.change_password_title),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                AppTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    placeholderText = stringResource(R.string.current_password),
                    isPassword = true,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    placeholderText = stringResource(R.string.new_password),
                    isPassword = true,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(oldPass, newPass)
                if (uiState.error != null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.change_password_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancel),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground
    )
}


@Preview(showBackground = true)
@Composable
fun ChangePasswordDialogPreview() {
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            ChangePasswordDialog(
                onDismiss = {},
                onConfirm = { _, _ -> },
                uiState = ProfileUiState(
                    fullName = "Preview User",
                    email = "preview@email.com",
                    error = "Error message"
                )
            )
        }
    }
}

