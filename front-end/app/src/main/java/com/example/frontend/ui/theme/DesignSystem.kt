package com.example.frontend.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

// -------------------- SPACING --------------------
object AppSpacing {
    val XS = 4.dp
    val S = 8.dp
    val M = 16.dp
    val L = 24.dp
    val XL = 32.dp
}

// -------------------- RADIUS --------------------
object AppRadius {
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
}

// -------------------- TYPOGRAPHY --------------------
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
// -------------------- ICON --------------------
object AppIconSize {
    val Tiny = 16.dp
    val Small = 20.dp
    val Medium = 24.dp
    val Large = 32.dp
    val ExtraLarge = 48.dp
}

// -------------------- ELEVATION --------------------
object AppElevation {
    val None = 0.dp
    val Small = 2.dp
    val Medium = 6.dp
    val Large = 12.dp
}

// -------------------- SHAPES --------------------
val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.Small),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.Medium),
    large = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.Large)
)

// -------------------- COLORS --------------------
object AppColor {
    val Primary = Color(0xFF1DB954)
    val Secondary = Color(0xFF191414)
    val Background = Color(0xFFF8F8F8)
    val Surface = Color(0xFFC7C6C6)
    val TextPrimary = Color(0xFF000000)
    val TextDarkMode = Color(0xFFF0F0F0)
    val TextSecondary = Color(0xFF666666)
    val Error = Color(0xFFFF3B30)
}

// -------------------- ANIMATION --------------------
object AppAnimation {
    const val Fast = 100
    const val Normal = 200
    const val Slow = 300
}

