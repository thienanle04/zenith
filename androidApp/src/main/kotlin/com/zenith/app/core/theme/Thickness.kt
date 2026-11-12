package com.zenith.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Stroke steps read as [MaterialTheme.thickness]. */
internal data class Thickness(
    val none: Dp = 0.dp,
    val light: Dp = 0.5.dp,
    val regular: Dp = 1.dp,
    val heavy: Dp = 2.dp,
)

internal val LocalThickness = compositionLocalOf<Thickness> { error("Thickness is provided by ZenithTheme") }

internal val MaterialTheme.thickness: Thickness
    @Composable
    @ReadOnlyComposable
    get() = LocalThickness.current
