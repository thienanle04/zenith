package com.zenith.app.features.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.zenith.app.R
import com.zenith.app.core.navigation.PortfolioDestination
import com.zenith.app.core.navigation.ZenithBottomBar
import com.zenith.app.core.theme.NegativeAmount
import com.zenith.app.core.theme.PositiveAmount
import com.zenith.app.core.utils.formatMinor
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.app.features.portfolio.components.PortfolioTopBar
import com.zenith.domain.portfolio.PortfolioView
import com.zenith.domain.portfolio.RowKind

@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel,
    onAddAccount: () -> Unit,
    onAddHolding: () -> Unit,
    onOpenAccount: (String) -> Unit,
    onOpenHolding: (String) -> Unit,
    onDelete: () -> Unit,
    onModifyOrders: () -> Unit,
    onSelectTab: (NavKey) -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val view = portfolio
    Scaffold(
        topBar = {
            PortfolioTopBar(
                onAddAccount = onAddAccount,
                onAddHolding = onAddHolding,
                onDelete = onDelete,
                onModifyOrders = onModifyOrders,
            )
        },
        bottomBar = { ZenithBottomBar(PortfolioDestination, onSelectTab) }
    ) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Top) {
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(8.dp), horizontalArrangement = Arrangement.SpaceAround,) {
                Figure(stringResource(R.string.assets), view.assets, view, PositiveAmount, Modifier.weight(1f))
                Figure(stringResource(R.string.liabilities), view.liabilities, view, NegativeAmount, Modifier.weight(1f))
                Figure(stringResource(R.string.net_worth), view.netWorth, view, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f), signed = true)
            }
            HorizontalDivider(thickness = .7.dp)
            PortfolioGroups(
                view = view,
                viewModel = viewModel,
                modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                onRow = { row ->
                    if (row.kind == RowKind.account) onOpenAccount(row.id) else onOpenHolding(row.id)
                },
            )
        }
    }
}

@Composable
private fun Figure(
    label: String,
    amount: Long,
    view: PortfolioView,
    color: Color,
    modifier: Modifier,
    signed: Boolean = false,
) {
    val number = formatMinor(kotlin.math.abs(amount), "VND", view.separators)
    val text =
        when {
            signed && amount < 0L -> "-$number"
            else -> number
        }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF737373),
            textAlign = TextAlign.Center,
        )
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
