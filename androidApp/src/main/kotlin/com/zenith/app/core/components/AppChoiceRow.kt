package com.zenith.app.core.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun <T> AppChoiceRow(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    enabled: Boolean = true,
    focusColor: Color = MaterialTheme.colorScheme.primary,
    onSelect: (T) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        AppSelect(
            value = value,
            options = options,
            optionLabel = optionLabel,
            onSelect = onSelect,
            modifier = Modifier.weight(4f),
            enabled = enabled,
            focusColor = focusColor,
        )
    }
}
