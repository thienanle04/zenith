package com.zenith.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Inset and gap steps read as [MaterialTheme.spacing]. */
internal data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val mediumSmall: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
)

internal val LocalSpacing = compositionLocalOf<Spacing> { error("Spacing is provided by ZenithTheme") }

internal val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
