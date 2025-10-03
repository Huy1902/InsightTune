package com.example.frontend.ui.profile

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.frontend.R


@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onNavigateLogin: () -> Unit,
    onBack: () -> Unit
) {

    val uiState by vm.uiState.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }
    var showPassDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showAvatarDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFFFF0000), // đỏ
                        0.3f to Color(0xFF8B0000), // đỏ sậm
                        0.6f to Color(0xFF000000)  // đen
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .systemBarsPadding()
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(25.dp)
                        .clickable {
                            onBack()
                        },
                    tint = Color.White
                )

                Text(
                    text = "Profile",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = uiState.avatarUrl ?: "",
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Gray)
                                //.clickable { showAvatarDialog = true }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${uiState.lastName} ${uiState.firstName}".ifBlank { "Your name" },
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Text(
                                text = "Role: ${uiState.role}",
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                fontSize = 14.sp
                            )

                            Text(
                                text = uiState.email,
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Serif,
                                fontSize = 14.sp
                            )

                            if (uiState.phone.isNotBlank()) {
                                Text(
                                    text = "Phone: ${uiState.phone}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 14.sp
                                )
                            }

                            if (uiState.address.isNotBlank()) {
                                Text(
                                    text = "Address: ${uiState.address}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileItem("Edit your profile") {
                        showProfileDialog = true
                    }

                    ProfileItem("Change your password") {
                        showPassDialog = true
                    }

                    ProfileItem("History") {
                        // TODO: Navigate sang HistoryScreen
                    }
                }

                if (showProfileDialog) {
                    ChangeProfileDialog(
                        uiState = uiState,
                        onDismiss = { showProfileDialog  = false },
                        onConfirm = { firstName, lastName, phone, address, role, avatarUri ->
                            vm.updateProfile(firstName, lastName, phone, address, role, avatarUri, context)
                        }
                    )
                }

                if (showPassDialog) {
                    ChangePasswordDialog(
                        onDismiss = { showPassDialog = false },
                        onConfirm = { oldPass, newPass ->
                            // TODO: call vm.changePassword(oldPass, newPass)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.height(8.dp))
                        }

                        if (uiState.error != null) {
                            Text(
                                text = uiState.error ?: "Error",
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        Button(
                            onClick = {
                                val refreshToken = vm.getRefreshToken() ?: ""
                                vm.logout(refreshToken) {
                                    onNavigateLogin()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier
                                .wrapContentWidth()
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "Log out",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = FontFamily.Serif
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    val context = LocalContext.current
    val sampleViewModel =
        remember { ProfileViewModel(context) }
    ProfileScreen(
        vm = sampleViewModel,
        onNavigateLogin = {
            println("Preview: Navigate to Login requested")
        },
        onBack = {
        }
    )
}


