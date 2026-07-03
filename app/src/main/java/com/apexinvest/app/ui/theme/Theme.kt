package com.apexinvest.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class AppColors(
    val trendGreen: Color,
    val trendRed: Color,
    val trendOrange: Color,
    val trendBlue: Color,
    val brandPurple: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        trendGreen = Color.Unspecified,
        trendRed = Color.Unspecified,
        trendOrange = Color.Unspecified,
        trendBlue = Color.Unspecified,
        brandPurple = Color.Unspecified
    )
}

private val LightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE6FA),
    onPrimaryContainer = Color(0xFF2E1A47),
    background = SurfaceLightBase,
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFDFDFE),
    onSurface = Color(0xFF1F1F24),
    surfaceVariant = Color(0xFFECEEF4),
    onSurfaceVariant = Color(0xFF2F2F36),
    secondary = BrandPurpleDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4DCFF),
    onSecondaryContainer = Color(0xFF2A1A66),
    error = TrendRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    primaryContainer = Color(0xFF423275),
    onPrimaryContainer = BrandPurpleLight,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    secondary = BrandPurpleLight,
    onSecondary = Color.Black,
    error = Color(0xFFFF585E),
    onError = Color.Black
)

@Composable
fun ApexInvestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val appColors = AppColors(
        trendGreen = if (darkTheme) TrendGreen else TrendGreenDark,
        trendRed = if (darkTheme) TrendRed else TrendRedDark,
        trendOrange = TrendOrange,
        trendBlue = TrendBlue,
        brandPurple = BrandPurple
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}
