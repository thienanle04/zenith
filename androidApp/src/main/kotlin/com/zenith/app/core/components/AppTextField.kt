package com.zenith.app.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.zenith.app.core.utils.canonicalAmount
import com.zenith.domain.portfolio.SeparatorPair

@Composable
fun AppTextField(
    value: String,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Text,
    focusColor: Color = MaterialTheme.colorScheme.primary,
    separators: SeparatorPair = SeparatorPair.commaPoint,
    prefix: String = "",
    suffix: String = "",
) {
    val number = keyboard == KeyboardType.Decimal || keyboard == KeyboardType.Number
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = FIELD_DISABLED_ALPHA)
    TextField(
        value = value,
        onValueChange = { raw -> onValue(if (number) canonicalAmount(raw, separators) else raw) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation =
            if (number) {
                AmountTransformation(separators, prefix, suffix)
            } else {
                VisualTransformation.None
            },
        shape = RectangleShape,
        colors =
            TextFieldDefaults.colors(
                focusedIndicatorColor = focusColor,
                unfocusedIndicatorColor = FieldResting,
                disabledIndicatorColor = disabled,
                errorIndicatorColor = FieldResting,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                cursorColor = if (number) Color.Transparent else focusColor,
            ),
    )
}
