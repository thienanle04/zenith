package com.zenith.app.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zenith.app.core.theme.spacing

@Composable
fun <T> AppChoiceRow(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    enabled: Boolean = true,
    onSelect: (T) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = FIELD_DISABLED_ALPHA)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { open = true }
                .padding(vertical = MaterialTheme.spacing.mediumSmall, horizontal = MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, color = if (enabled) MaterialTheme.colorScheme.primary else disabled)
    }
    if (open && enabled) {
        ChoiceDialog(
            value = value,
            options = options,
            optionLabel = optionLabel,
            onSelect = { option ->
                open = false
                onSelect(option)
            },
            onDismiss = { open = false },
        )
    }
}
