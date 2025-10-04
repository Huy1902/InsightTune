package com.example.frontend.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                "Change your avatar",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    "📷 Take a photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTakePhoto()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "🖼️ Choose from gallery",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPickGallery()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White, fontFamily = FontFamily.Serif)
            }
        },
        containerColor = Color(0xFF121212)
    )
}
