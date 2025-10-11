package com.example.frontend.ui.profile

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.R

@Composable
fun ChangeAvatarDialog(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.change_avatar),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.camera),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTakePhoto()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    fontSize = 16.  sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.gallery),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPickGallery()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancel),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChangeAvatarDialogPreview() {
    AppTheme {
        ChangeAvatarDialog(onDismiss = {}, onPickGallery = {}, onTakePhoto = {})
    }
}
