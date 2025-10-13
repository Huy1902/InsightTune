package com.example.frontend.ui.profile

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.frontend.core.AppPreferences
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.R

@Composable
fun LanguageSelectionDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val prefs = remember { AppPreferences(context) }
    var currentLanguage by remember { mutableStateOf(prefs.getLanguage()) }

    val onLanguageSelected: (String) -> Unit = { langCode ->
        prefs.saveLanguage(langCode)
        currentLanguage = langCode
//        onDismissRequest()
        activity?.recreate()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                stringResource(R.string.language),
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("vi") }
                ) {
                    RadioButton(
                        selected = currentLanguage == "vi",
                        onClick = { onLanguageSelected("vi") }
                    )
                    Text("Tiếng Việt")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("en") }
                ) {
                    RadioButton(
                        selected = currentLanguage == "en",
                        onClick = { onLanguageSelected("en") }
                    )
                    Text("English")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground
    )
}