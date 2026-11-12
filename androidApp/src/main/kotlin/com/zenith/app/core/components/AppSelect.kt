package com.zenith.app.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zenith.app.core.theme.spacing

@Composable
fun <T> AppSelect(
    value: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var open by remember { mutableStateOf(false) }
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = FIELD_DISABLED_ALPHA)
    Text(
        value,
        modifier = modifier.clickable(enabled = enabled) { open = true },
        color = if (enabled) MaterialTheme.colorScheme.primary else disabled,
    )
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

@Composable
internal fun <T> ChoiceDialog(
    value: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                Modifier
                    .widthIn(min = 280.dp, max = 400.dp)
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = MaterialTheme.spacing.small),
            ) {
                options.forEach { option ->
                    val text = optionLabel(option)
                    Text(
                        text,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(option) }
                                .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.medium),
                        color = if (text == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
