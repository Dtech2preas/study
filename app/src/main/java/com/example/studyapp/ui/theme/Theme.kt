package com.example.studyapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Force a dark premium look everywhere, regardless of system theme
private val PremiumColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = PremiumBlack,
    primaryContainer = DeepBlue,
    onPrimaryContainer = TextWhite,

    secondary = MutedBlue,
    onSecondary = PremiumBlack,
    secondaryContainer = SurfaceVariantBlack,
    onSecondaryContainer = TextWhite,

    tertiary = ElectricBlue,
    onTertiary = PremiumBlack,

    background = PremiumBlack,
    onBackground = TextWhite,

    surface = SurfaceBlack,
    onSurface = TextWhite,
    surfaceVariant = SurfaceVariantBlack,
    onSurfaceVariant = TextGray,

    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    onError = PremiumBlack
)

@Composable
fun StudyAppTheme(
    // We intentionally ignore these flags to force the premium dark theme
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = PremiumColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
