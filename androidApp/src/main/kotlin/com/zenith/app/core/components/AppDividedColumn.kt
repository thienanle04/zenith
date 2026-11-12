package com.zenith.app.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.zenith.app.core.theme.thickness

/** Rows with a divider above the group. [middleDividers] draws a divider between rows. A divider below the last row follows [trailingDivider]. */
@Composable
fun <T> AppDividedColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    middleDividers: Boolean = true,
    trailingDivider: Boolean = true,
    thickness: Dp = MaterialTheme.thickness.light,
    itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    Column(modifier) {
        HorizontalDivider(thickness = thickness)
        items.forEachIndexed { index, item ->
            itemContent(index, item)
            val last = index == items.lastIndex
            if ((middleDividers && !last)) {
                HorizontalDivider(thickness = thickness)
            }
        }
        if (trailingDivider) HorizontalDivider(thickness = thickness)
    }
}
