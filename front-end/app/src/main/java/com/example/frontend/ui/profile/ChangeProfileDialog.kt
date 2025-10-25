package com.example.frontend.ui.profile

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.frontend.ui.common.AppTextField
import com.example.frontend.ui.profile.ProfileUiState
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeProfileDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onConfirm: (
        String, // firstName
        String, // lastName
        String, // phone
        String, // address
        String, // role
    ) -> Unit
) {
    var firstName by remember { mutableStateOf(uiState.firstName) }
    var lastName by remember { mutableStateOf(uiState.lastName) }
    var phone by remember { mutableStateOf(uiState.phone) }
    var address by remember { mutableStateOf(uiState.address) }
    var role by remember { mutableStateOf(uiState.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.edit_profile),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(firstName, { firstName = it }, placeholderText = stringResource(R.string.first_name))
                AppTextField(lastName, { lastName = it }, placeholderText = stringResource(R.string.last_name))
                AppTextField(phone, { phone = it }, placeholderText = stringResource(R.string.phone))
                AppTextField(address, { address = it }, placeholderText = stringResource(R.string.address))

                var expanded by remember { mutableStateOf(false) }

//                ExposedDropdownMenuBox(
//                    expanded = expanded,
//                    onExpandedChange = { expanded = !expanded }
//                ) {
//                    AppTextField(
//                        value = role,
//                        onValueChange = {},
//                        readOnly = true,
//                        placeholderText = stringResource(R.string.select_role),
//                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .menuAnchor()
//                            .clickable { expanded = true }
//                    )
//
//                    ExposedDropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false },
//                        containerColor = MaterialTheme.colorScheme.surfaceVariant
//                    ) {
//                        DropdownMenuItem(
//                            text = { Text("USER") },
//                            onClick = {
//                                role = "USER"
//                                expanded = false
//                            },
//                            enabled = uiState.role != "ADMIN"
//                        )
//                        DropdownMenuItem(
//                            text = { Text("ADMIN") },
//                            onClick = {
//                                role = "ADMIN"
//                                expanded = false
//                            }
//                        )
//                    }
//                }
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
                onConfirm(firstName, lastName, phone, address, role)
                if (uiState.error != null) {
                    onDismiss()
                }
            }) {
                Text(
                    stringResource(R.string.save),
                )
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
