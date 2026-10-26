package com.gcoded.meditrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = SlateTeal40,
    onSecondary = Color.White,
    secondaryContainer = SlateTeal90,
    onSecondaryContainer = SlateTeal20,
    tertiary = Blue40,
    onTertiary = Color.White,
    tertiaryContainer = Blue90,
    onTertiaryContainer = Blue20,
    background = NeutralLightSurface,
    onBackground = NeutralLightOnSurface,
    surface = NeutralLightSurface,
    onSurface = NeutralLightOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal10,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = SlateTeal80,
    onSecondary = SlateTeal20,
    secondaryContainer = SlateTeal30,
    onSecondaryContainer = SlateTeal90,
    tertiary = Blue80,
    onTertiary = Blue20,
    tertiaryContainer = Blue30,
    onTertiaryContainer = Blue90,
    background = NeutralDarkSurface,
    onBackground = NeutralDarkOnSurface,
    surface = NeutralDarkSurface,
    onSurface = NeutralDarkOnSurface
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = AppTypography,
        content = content
    )
}
