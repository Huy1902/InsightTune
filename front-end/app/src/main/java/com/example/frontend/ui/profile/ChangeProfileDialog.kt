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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.frontend.ui.profile.ProfileUiState

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
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    var showAvatarOptions by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Profile",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // First name
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("Change first name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Last name
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Change last name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Phone
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("Change phone number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Address
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = { Text("Change address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Role
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { expanded = true }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color(0xFF121212)
                    ) {
                        DropdownMenuItem(
                            text = { Text("USER", color = Color.White) },
                            onClick = {
                                role = "USER"
                                expanded = false
                            },
                            enabled = uiState.role != "ADMIN"
                        )
                        DropdownMenuItem(
                            text = { Text("ADMIN", color = Color.White) },
                            onClick = {
                                role = "ADMIN"
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(firstName, lastName, phone, address, role)
                onDismiss()
            }) {
                Text("Save", color = Color.Red, fontFamily = FontFamily.Serif)
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
