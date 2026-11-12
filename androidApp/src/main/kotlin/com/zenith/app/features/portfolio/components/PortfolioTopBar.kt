package com.zenith.app.features.portfolio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zenith.app.R
import com.zenith.app.core.theme.spacing

@Composable
fun PortfolioTopBar(
    onAddAccount: () -> Unit,
    onAddHolding: () -> Unit,
    onDelete: () -> Unit,
    onModifyOrders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menu by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .height(64.dp)
            .padding(horizontal = MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.portfolio),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
        )
        Box {
            val menuLabel = stringResource(R.string.menu)
            IconButton(onClick = { menu = true }, modifier = Modifier.semantics { contentDescription = menuLabel }) {
                Text("⋮", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
            DropdownMenu(expanded = menu, onDismissRequest = {
                menu = false
                adding = false
            }) {
                if (adding) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_account)) },
                        onClick = {
                            menu = false
                            adding = false
                            onAddAccount()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_holding)) },
                        onClick = {
                            menu = false
                            adding = false
                            onAddHolding()
                        },
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add)) },
                        onClick = { adding = true },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            menu = false
                            onDelete()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.modify_orders)) },
                        onClick = {
                            menu = false
                            onModifyOrders()
                        },
                    )
                }
            }
        }
    }
}
