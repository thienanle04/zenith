package com.zenith.app.core.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PositiveAmount = Color(0xFF1E88E5)
val NegativeAmount = Color(0xFFFC6456)

val AppRed = Color(0xFFF55346)

internal val colors =
    lightColorScheme(
        background = Color.Transparent,
        primary = AppRed,
        onPrimary = Color.White,
        primaryContainer = AppRed,
        secondary = AppRed,
        secondaryContainer = Color.Transparent,
        onSecondaryContainer = AppRed,
        surface = Color.Transparent,
        surfaceContainer = Color.White,
        onSurface = Color.Black,
        onSurfaceVariant = Color.Gray,
    )
