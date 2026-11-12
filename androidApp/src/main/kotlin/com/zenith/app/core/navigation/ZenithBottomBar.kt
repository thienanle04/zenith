package com.zenith.app.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.zenith.app.R
import com.zenith.app.core.theme.thickness

@Composable
fun ZenithBottomBar(
    current: NavKey,
    onSelect: (NavKey) -> Unit,
) {
    Column(verticalArrangement = Arrangement.SpaceAround) {
        HorizontalDivider(
            thickness = MaterialTheme.thickness.light,
        )
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Tab(TransactionsDestination, current, Icons.Outlined.Receipt, R.string.transactions, onSelect)
                Tab(StatsDestination, current, Icons.Outlined.BarChart, R.string.stats, onSelect)
                Tab(PortfolioDestination, current, Icons.Outlined.AccountBalanceWallet, R.string.portfolio, onSelect)
                Tab(MoreDestination, current, Icons.Outlined.MoreHoriz, R.string.more, onSelect)
            }
        }
    }
}

@Composable
private fun RowScope.Tab(
    destination: NavKey,
    current: NavKey,
    icon: ImageVector,
    label: Int,
    onSelect: (NavKey) -> Unit,
) {
    NavigationBarItem(
        selected = current == destination,
        onClick = { onSelect(destination) },
        icon = { Icon(icon, contentDescription = null, modifier = Modifier) },
        label = { Text(stringResource(label)) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
    )
}
