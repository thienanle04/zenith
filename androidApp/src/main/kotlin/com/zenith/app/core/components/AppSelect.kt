package com.zenith.app.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> AppSelect(
    value: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusColor: Color = MaterialTheme.colorScheme.primary,
) {
    var expanded by remember { mutableStateOf(false) }
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = FIELD_DISABLED_ALPHA)
    val open = expanded && enabled
    val lineColor =
        when {
            !enabled -> disabled
            open -> focusColor
            else -> FieldResting
        }
    val stroke = if (open) 2.dp else 1.dp
    Box(modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .drawBehind {
                        val width = stroke.toPx()
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, size.height - width / 2f),
                            end = Offset(size.width, size.height - width / 2f),
                            strokeWidth = width,
                        )
                    }.clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(value, color = if (enabled) Color.Unspecified else disabled)
        }
        DropdownMenu(expanded = open, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
