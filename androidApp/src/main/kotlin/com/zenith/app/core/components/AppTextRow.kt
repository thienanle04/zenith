package com.zenith.app.core.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import com.zenith.domain.portfolio.SeparatorPair

@Composable
fun AppTextRow(
    label: String,
    value: String,
    keyboard: KeyboardType = KeyboardType.Text,
    focusColor: Color = MaterialTheme.colorScheme.primary,
    separators: SeparatorPair = SeparatorPair.commaPoint,
    prefix: String = "",
    suffix: String = "",
    onValue: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        AppTextField(
            value = value,
            onValue = onValue,
            modifier = Modifier.weight(4f),
            keyboard = keyboard,
            focusColor = focusColor,
            separators = separators,
            prefix = prefix,
            suffix = suffix,
        )
    }
}
