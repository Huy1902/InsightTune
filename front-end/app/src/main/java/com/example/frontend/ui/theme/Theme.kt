package com.example.frontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 🎨 SPO-TUBE APP THEME
 * Kết hợp MaterialTheme + hệ thống design riêng (AppSpacing, AppIconSize, v.v.)
 * Hỗ trợ light / dark mode.
 */

// -------------------- 🎨 COLOR SCHEME --------------------
private val LightColors = lightColorScheme(
    primary = AppColor.Primary,
    secondary = AppColor.Secondary,
    background = AppColor.Background,
    surface = AppColor.Surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = AppColor.Primary,
    secondary = AppColor.Secondary,
    background = Color(0xFF191414),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

// -------------------- 🧩 LOCAL PROVIDERS --------------------
private val LocalAppSpacing = staticCompositionLocalOf { AppSpacing }
private val LocalAppRadius = staticCompositionLocalOf { AppRadius }
private val LocalAppIconSize = staticCompositionLocalOf { AppIconSize }
private val LocalAppElevation = staticCompositionLocalOf { AppElevation }
private val LocalAppColor = staticCompositionLocalOf { AppColor }
private val LocalAppAnimation = staticCompositionLocalOf { AppAnimation }

// -------------------- 🌈 APP THEME COMPOSABLE --------------------
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(
        LocalAppSpacing provides AppSpacing,
        LocalAppRadius provides AppRadius,
        LocalAppIconSize provides AppIconSize,
        LocalAppElevation provides AppElevation,
        LocalAppColor provides AppColor,
        LocalAppAnimation provides AppAnimation
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}

// -------------------- 🔗 ACCESS OBJECT --------------------
object AppTheme {

    @Composable
    fun color(): AppColor = LocalAppColor.current

    @Composable
    fun spacing(): AppSpacing = LocalAppSpacing.current

    @Composable
    fun radius(): AppRadius = LocalAppRadius.current

    @Composable
    fun iconSize(): AppIconSize = LocalAppIconSize.current

    @Composable
    fun elevation(): AppElevation = LocalAppElevation.current

    @Composable
    fun animation(): AppAnimation = LocalAppAnimation.current

    val typography get() = Typography
}
