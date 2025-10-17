package com.example.frontend.ui.profile

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.R

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPass: String, newPass: String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var isOldPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Change your password",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                TextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    placeholder = { Text("Enter current password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    visualTransformation = if (isOldPasswordVisible) VisualTransformation.None
                    else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { isOldPasswordVisible = !isOldPasswordVisible }
                        ) {
                            Icon(
                                painter = if (isOldPasswordVisible)
                                    painterResource(id = R.drawable.opened_eye)
                                else
                                    painterResource(id = R.drawable.closed_eye),
                                contentDescription = if (isOldPasswordVisible ) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    placeholder = { Text("Enter new password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    trailingIcon = {
                        IconButton(
                            onClick = { isNewPasswordVisible = !isNewPasswordVisible }
                        ) {
                            Icon(
                                painter = if (isNewPasswordVisible)
                                    painterResource(id = R.drawable.opened_eye)
                                else
                                    painterResource(id = R.drawable.closed_eye),
                                contentDescription = if (isNewPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(oldPass, newPass)
                onDismiss()
            }) {
                Text("OK", color = Color.Red, fontFamily = FontFamily.Serif)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White, fontFamily = FontFamily.Serif)
            }
        },
        containerColor = Color(0xFF121212)
    )
}



@Preview(showBackground = true)
@Composable
fun ChangePasswordDialogPreview() {
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            ChangePasswordDialog(
                onDismiss = {},
                onConfirm = { _, _ -> }
            )
        }
    }
}

