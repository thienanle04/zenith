package com.zenith.app.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val Roboto =
    FontFamily(
        Font(DeviceFontFamilyName("Roboto"), FontWeight.Normal),
        Font(DeviceFontFamilyName("sans-serif"), FontWeight.Normal),
        Font(DeviceFontFamilyName("Roboto"), FontWeight.Medium),
        Font(DeviceFontFamilyName("sans-serif"), FontWeight.Medium),
        Font(DeviceFontFamilyName("Roboto"), FontWeight.Bold),
        Font(DeviceFontFamilyName("sans-serif"), FontWeight.Bold),
    )

private fun TextStyle.roboto() = copy(fontFamily = Roboto)

internal val typography =
    Typography().run {
        Typography(
            displayLarge = displayLarge.roboto(),
            displayMedium = displayMedium.roboto(),
            displaySmall = displaySmall.roboto(),
            headlineLarge = headlineLarge.roboto(),
            headlineMedium = headlineMedium.roboto(),
            headlineSmall = headlineSmall.roboto(),
            titleLarge = titleLarge.roboto(),
            titleMedium = titleMedium.roboto(),
            titleSmall = titleSmall.roboto(),
            bodyLarge = bodyLarge.roboto(),
            bodyMedium = bodyMedium.roboto(),
            bodySmall = bodySmall.roboto(),
            labelLarge = labelLarge.roboto(),
            labelMedium = labelMedium.roboto(),
            labelSmall = labelSmall.roboto(),
        )
    }
