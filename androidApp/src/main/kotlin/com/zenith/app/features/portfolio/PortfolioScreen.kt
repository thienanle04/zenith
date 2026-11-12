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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.zenith.app.R
import com.zenith.app.core.navigation.PortfolioDestination
import com.zenith.app.core.navigation.ZenithBottomBar
import com.zenith.app.core.preview.SamplePortfolio
import com.zenith.app.core.theme.NegativeAmount
import com.zenith.app.core.theme.PositiveAmount
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing
import com.zenith.app.core.theme.thickness
import com.zenith.app.core.utils.formatShown
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.app.features.portfolio.components.PortfolioTopBar
import com.zenith.domain.money.Exact
import com.zenith.domain.money.abs
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioView
import com.zenith.domain.portfolio.RowKind
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    PortfolioScreen(
        view = portfolio,
        onAddAccount = onAddAccount,
        onAddHolding = onAddHolding,
        onOpenAccount = onOpenAccount,
        onOpenHolding = onOpenHolding,
        onDelete = onDelete,
        onModifyOrders = onModifyOrders,
        onSelectTab = onSelectTab,
        onMoveSection = { key, delta -> scope.launch { viewModel.commit(PortfolioCommand.MoveSection(key, delta)) } },
        onMoveRow = { sectionKey, rowId, delta ->
            scope.launch { viewModel.commit(PortfolioCommand.MoveRow(sectionKey, rowId, delta)) }
        },
    )
}

@Composable
private fun PortfolioScreen(
    view: PortfolioView?,
    onAddAccount: () -> Unit,
    onAddHolding: () -> Unit,
    onOpenAccount: (String) -> Unit,
    onOpenHolding: (String) -> Unit,
    onDelete: () -> Unit,
    onModifyOrders: () -> Unit,
    onSelectTab: (NavKey) -> Unit,
    onMoveSection: (String, Int) -> Unit,
    onMoveRow: (String, String, Int) -> Unit,
) {
    Scaffold(
        topBar = {
            PortfolioTopBar(
                onAddAccount = onAddAccount,
                onAddHolding = onAddHolding,
                onDelete = onDelete,
                onModifyOrders = onModifyOrders,
            )
        },
        bottomBar = { ZenithBottomBar(PortfolioDestination, onSelectTab) },
    ) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(MaterialTheme.spacing.large))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Top) {
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(MaterialTheme.spacing.small), horizontalArrangement = Arrangement.SpaceAround) {
                Figure(stringResource(R.string.assets), view.assetsExact, view, PositiveAmount, Modifier.weight(1f))
                Figure(stringResource(R.string.liabilities), view.liabilitiesExact, view, NegativeAmount, Modifier.weight(1f))
                Figure(stringResource(R.string.net_worth), view.netWorthExact, view, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f), signed = true)
            }
            HorizontalDivider(thickness = MaterialTheme.thickness.regular)
            PortfolioGroups(
                view = view,
                modifier = Modifier.weight(1f).padding(vertical = MaterialTheme.spacing.medium),
                onRow = { row ->
                    if (row.kind == RowKind.account) onOpenAccount(row.id) else onOpenHolding(row.id)
                },
                onMoveSection = onMoveSection,
                onMoveRow = onMoveRow,
            )
        }
    }
}

@Composable
private fun Figure(
    label: String,
    amount: Exact,
    view: PortfolioView,
    color: Color,
    modifier: Modifier,
    signed: Boolean = false,
) {
    val precision = view.amountPrecisions[view.primaryCurrency] ?: 0
    val number = formatShown(if (signed) amount else amount.abs(), view.primaryCurrency, view.separators, precision)
    val text = number
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PortfolioScreenPreview() {
    ZenithTheme {
        PortfolioScreen(
            view = SamplePortfolio.view,
            onAddAccount = {},
            onAddHolding = {},
            onOpenAccount = {},
            onOpenHolding = {},
            onDelete = {},
            onModifyOrders = {},
            onSelectTab = {},
            onMoveSection = { _, _ -> },
            onMoveRow = { _, _, _ -> },
        )
    }
}
