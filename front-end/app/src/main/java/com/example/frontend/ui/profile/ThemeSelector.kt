package com.example.frontend.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.ui.theme.ThemeSetting
import com.example.frontend.R

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                stringResource(R.string.theme_selection),
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            ThemeSelectorContent(
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close))
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ThemeSelectorContent(
    currentTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    Column {
        ThemeSetting.values().forEach { theme ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(theme) }
                    .padding(vertical = AppTheme.spacing().XS)
            ) {
                RadioButton(
                    selected = currentTheme == theme,
                    onClick = { onThemeSelected(theme) },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(AppTheme.spacing().S))
                Text(
                    text = when (theme) {
                        ThemeSetting.LIGHT -> stringResource(R.string.light)
                        ThemeSetting.DARK -> stringResource(R.string.dark)
                        ThemeSetting.SYSTEM -> stringResource(R.string.system)
                    },
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}