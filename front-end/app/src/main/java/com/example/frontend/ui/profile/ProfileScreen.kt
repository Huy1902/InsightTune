package com.example.frontend.ui.profile

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.core.SessionManager
import com.example.frontend.service.MusicService
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.ui.theme.ThemeSetting
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


@OptIn(UnstableApi::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onNavigateLogin: () -> Unit,
    onBack: () -> Unit,
    themeSetting: ThemeSetting,
    onThemeChange: (ThemeSetting) -> Unit
) {

    val uiState by vm.uiState.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }
    var showThemeSelectorDialog by remember { mutableStateOf(false) }
    var showPassDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.updateAvatar(it, context) }
    }

    val cameraUri = remember {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "avatar_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            vm.updateAvatar(cameraUri!!, context)
        }
    }

//    LaunchedEffect(uiState.error == null) {
//        Toast.makeText(context, context.getString(R.string.update_profile_successfully), Toast.LENGTH_SHORT).show()
//    }

    ProfileScreenContent(
        uiState = uiState,
        themeSetting = themeSetting,
        onBackClick = onBack,
        onLogoutClick = {
            val stopIntent = Intent(context, MusicService::class.java).apply {
                action = MusicService.ACTION_STOP_SERVICE
            }
            context.startService(stopIntent)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("707539669485-hvb782jf95k54qogpjmcalbjiiec9nrd.apps.googleusercontent.com")
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            googleSignInClient.signOut().addOnCompleteListener {
            }
            vm.clearLocalTokens()
            onNavigateLogin()
        },
        onAvatarClick = { showAvatarDialog = true },
        onEditProfileClick = { showProfileDialog = true },
        onChangePasswordClick = { showPassDialog = true },
        onHistoryClick = { /* TODO */ },
        onChangeThemeClick = { showThemeSelectorDialog = true },
        onLanguageClick = { showLanguageDialog = true }
    )

    if (showAvatarDialog) {
        ChangeAvatarDialog(
            onDismiss = { showAvatarDialog = false },
            onPickGallery = { galleryLauncher.launch("image/*") },
            onTakePhoto = { cameraUri?.let { cameraLauncher.launch(it) } }
        )
    }
    if (showProfileDialog) {
        ChangeProfileDialog(
            uiState = uiState,
            onDismiss = { showProfileDialog = false },
            onConfirm = { firstName, lastName, phone, address, role ->
                vm.updateProfile(firstName, lastName, address, phone, role)
            }
        )
    }
    if (showPassDialog) {
        ChangePasswordDialog(
            onDismiss = { showPassDialog = false },
            onConfirm = { oldPass, newPass -> vm.changePassword(oldPass, newPass) },
            uiState = uiState
        )
    }
    if (showThemeSelectorDialog) {
        ThemeSelectionDialog(
            currentTheme = themeSetting,
            onThemeSelected = onThemeChange,
            onDismissRequest = { showThemeSelectorDialog = false }
        )
    }
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismissRequest = { showLanguageDialog = false }
        )
    }
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    themeSetting: ThemeSetting,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChangeThemeClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing().M)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackClick() },
                tint = MaterialTheme.colorScheme.onBackground
            )
            Text(
                stringResource(R.string.profile),
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(AppTheme.spacing().M)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = uiState.avatarUrl ?: R.drawable.spotube_cropped,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(81.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarClick)
                )
                Spacer(modifier = Modifier.width(AppTheme.spacing().M))
                Column {
                    Text(
                        text = uiState.fullName.ifBlank { "Your name" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
//                    if (uiState.phone.isNotBlank()) {
//                        Text(
//                            stringResource(R.string.phone, uiState.phone),
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onBackground
//                        )
//                    }
//                    if (uiState.address.isNotBlank()) {
//                        Text(
//                            stringResource(R.string.address, uiState.address),
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onBackground
//                        )
//                    }
//                    Text(
//                        stringResource(R.string.role, uiState.role),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
                }
            }
            Spacer(modifier = Modifier.height(AppTheme.spacing().L))
            ProfileItem(
                text = stringResource(R.string.edit_profile),
                onEditProfileClick
            )
            ProfileItem(
                stringResource(R.string.change_password),
                onChangePasswordClick
            )
//            ProfileItem(
//                stringResource(R.string.history),
//                onHistoryClick
//            )
            ProfileItem(
                stringResource(R.string.change_theme),
                    onChangeThemeClick
                )
            ProfileItem(
                stringResource(R.string.language),
                onLanguageClick
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing().M),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.height(AppTheme.spacing().S))
            }
//            if (uiState.error != null) {
//                Text(
//                    text = uiState.error,
//                    color = MaterialTheme.colorScheme.error,
//                    fontSize = 12.sp,
//                    modifier = Modifier.padding(bottom = AppTheme.spacing().S)
//                )
//            }
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .height(56.dp)
            ) {
                Text(
                    stringResource(R.string.log_out),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
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
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Preview(name = "Light Mode", showSystemUi = true)
@Preview(name = "Dark Mode", showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ProfilePreview() {
    var theme by remember { mutableStateOf(ThemeSetting.DARK) }
    AppTheme(darkTheme = theme == ThemeSetting.DARK) {
        ProfileScreenContent(
            uiState = ProfileUiState(
                fullName = "Preview User",
                email = "preview@email.com",
                error = "Error message"
            ),
            themeSetting = theme,
            onBackClick = {},
            onLogoutClick = {},
            onAvatarClick = {},
            onEditProfileClick = {},
            onChangePasswordClick = {},
            onHistoryClick = {},
            onChangeThemeClick = {},
            onLanguageClick = {}
        )
    }
}


