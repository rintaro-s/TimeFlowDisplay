package com.nbks.mi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MiColors.Primary,
    onPrimary = MiColors.OnPrimary,
    primaryContainer = MiColors.PrimaryContainer,
    onPrimaryContainer = MiColors.OnPrimaryContainer,
    secondary = MiColors.Secondary,
    onSecondary = MiColors.OnSecondary,
    secondaryContainer = MiColors.SecondaryContainer,
    onSecondaryContainer = MiColors.OnSecondaryContainer,
    tertiary = MiColors.Tertiary,
    onTertiary = MiColors.OnTertiary,
    tertiaryContainer = MiColors.TertiaryContainer,
    onTertiaryContainer = MiColors.OnTertiaryContainer,
    error = MiColors.Error,
    onError = MiColors.OnError,
    errorContainer = MiColors.ErrorContainer,
    onErrorContainer = MiColors.OnErrorContainer,
    background = MiColors.Background,
    onBackground = MiColors.OnBackground,
    surface = MiColors.Surface,
    onSurface = MiColors.OnSurface,
    surfaceVariant = MiColors.SurfaceVariant,
    onSurfaceVariant = MiColors.OnSurfaceVariant,
    outline = MiColors.Outline,
    outlineVariant = MiColors.OutlineVariant,
    inverseSurface = MiColors.InverseSurface,
    inverseOnSurface = MiColors.InverseOnSurface,
    inversePrimary = MiColors.InversePrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = MiColors.PrimaryLight,
    onPrimary = MiColors.OnPrimaryLight,
    primaryContainer = MiColors.PrimaryContainerLight,
    onPrimaryContainer = MiColors.OnPrimaryContainerLight,
    secondary = MiColors.SecondaryLight,
    onSecondary = MiColors.OnSecondaryLight,
    secondaryContainer = MiColors.SecondaryContainerLight,
    onSecondaryContainer = MiColors.OnSecondaryContainerLight,
    tertiary = MiColors.TertiaryLight,
    onTertiary = MiColors.OnTertiaryLight,
    tertiaryContainer = MiColors.TertiaryContainerLight,
    onTertiaryContainer = MiColors.OnTertiaryContainerLight,
    background = MiColors.BackgroundLight,
    onBackground = MiColors.OnBackgroundLight,
    surface = MiColors.SurfaceLight,
    onSurface = MiColors.OnSurfaceLight,
    surfaceVariant = MiColors.SurfaceVariantLight,
    onSurfaceVariant = MiColors.OnSurfaceVariantLight,
    outline = MiColors.OutlineLight,
    outlineVariant = MiColors.OutlineVariantLight,
)

@Composable
fun MiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            window?.let {
                it.statusBarColor = Color.Transparent.value.toInt()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}