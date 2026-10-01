package com.jetwatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Sky,
    onPrimary = Navy,
    secondary = Amber,
    onSecondary = Navy,
    tertiary = Amber,
    background = Navy,
    surface = Ink,
    onBackground = Foam,
    onSurface = Foam,
)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Foam,
    secondary = Amber,
    onSecondary = Navy,
    tertiary = Sky,
    background = Foam,
    surface = Color(0xFFFFFFFF),
    onBackground = Navy,
    onSurface = Navy,
)

@Composable
fun JetWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = platformColorScheme(darkTheme, dynamicColor)
        ?: if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
expect fun platformColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme?
