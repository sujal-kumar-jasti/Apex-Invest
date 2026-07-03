package com.apexinvest.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
<<<<<<< HEAD
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
=======
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --------------------
// Brand Colors
// --------------------
val PurpleBrand = Color(0xFF7A41C4)
val PurpleLight = Color(0xFFD6CBF3)
val PurpleDark = Color(0xFF5E35B1)

val RedLossLight = Color(0xFFFF6B6B)

// --------------------
// LIGHT THEME (IMPROVED)
// --------------------
private val LightColorScheme = lightColorScheme(

    // Brand
    primary = PurpleBrand,
    onPrimary = Color.White,

    primaryContainer = Color(0xFFEDE6FA),
    onPrimaryContainer = Color(0xFF2E1A47),

    // App background (very light grey, NOT white)
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF1B1B1F),

    // Scaffold / main surfaces (slightly darker than background)
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    surface = Color(0xFFFDFDFE),
    onSurface = Color(0xFF1F1F24),
    surfaceVariant = Color(0xFFECEEF4),
    onSurfaceVariant = Color(0xFF2F2F36),
<<<<<<< HEAD
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
=======

    secondary = PurpleDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4DCFF),
    onSecondaryContainer = Color(0xFF2A1A66),

    error = RedLossLight,
    onError = Color.White
)

// --------------------
// DARK THEME (UNCHANGED – ALREADY GOOD)
// --------------------
private val DarkColorScheme = darkColorScheme(
    primary = PurpleBrand,
    onPrimary = Color.White,

    background = Color(0xFF000000),
    onBackground = Color.White,

    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,

    primaryContainer = Color(0xFF423275),
    onPrimaryContainer = PurpleLight,

    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color.White,

    secondary = PurpleLight,
    onSecondary = Color.Black,

>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    error = Color(0xFFFF585E),
    onError = Color.Black
)

<<<<<<< HEAD
=======
// --------------------
// THEME WRAPPER
// --------------------
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
@Composable
fun ApexInvestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
<<<<<<< HEAD
    
    val appColors = AppColors(
        trendGreen = if (darkTheme) TrendGreen else TrendGreenDark,
        trendRed = if (darkTheme) TrendRed else TrendRedDark,
        trendOrange = TrendOrange,
        trendBlue = TrendBlue,
        brandPurple = BrandPurple
    )
=======
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
<<<<<<< HEAD
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
=======
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // your Typography.kt
        content = content
    )
}
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
