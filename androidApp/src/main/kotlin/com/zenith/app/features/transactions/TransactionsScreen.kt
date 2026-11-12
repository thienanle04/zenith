package com.zenith.app.features.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.zenith.app.core.navigation.TransactionsDestination
import com.zenith.app.core.navigation.ZenithBottomBar
import com.zenith.app.core.preview.SamplePortfolio
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing
import com.zenith.app.core.utils.formatQuantity
import com.zenith.app.core.utils.formatShown
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.app.features.portfolio.amountColor
import com.zenith.app.features.transactions.components.TransactionsTopBar
import com.zenith.domain.money.Exact
import com.zenith.domain.money.abs
import com.zenith.domain.portfolio.PortfolioView
import com.zenith.domain.portfolio.TransactionLine

@Composable
fun TransactionsScreen(
    viewModel: PortfolioViewModel,
    onSelectTab: (NavKey) -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    TransactionsScreen(portfolio, onSelectTab)
}

@Composable
private fun TransactionsScreen(
    view: PortfolioView?,
    onSelectTab: (NavKey) -> Unit,
) {
    Scaffold(
        topBar = { TransactionsTopBar() },
        bottomBar = { ZenithBottomBar(TransactionsDestination, onSelectTab) },
    ) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(MaterialTheme.spacing.large))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding).padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            LazyColumn(Modifier.weight(1f)) {
                items(view.transactions, key = { it.id }) { line ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(line.title, modifier = Modifier.weight(1f))
                        Text(lineFigure(line, view), color = amountColor(lineSign(line)))
                    }
                }
            }
        }
    }
}

private fun lineFigure(
    line: TransactionLine,
    view: PortfolioView,
): String {
    val quantity = line.quantity
    if (quantity != null) {
        return formatQuantity(quantity, line.unit ?: "", view.separators)
    }
    val currency = line.currency ?: "VND"
    val amount = line.amount ?: Exact.zero
    return formatShown(amount.abs(), currency, view.separators, view.amountPrecisions[currency] ?: 0)
}

private fun lineSign(line: TransactionLine): Long {
    val quantity = line.quantity
    return if (quantity != null) quantity.sign.toLong() else (line.amount ?: Exact.zero).sign.toLong()
}

private fun Long.sign(): Long =
    when {
        this > 0L -> 1L
        this < 0L -> -1L
        else -> 0L
    }

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TransactionsScreenPreview() {
    ZenithTheme {
        TransactionsScreen(SamplePortfolio.view) {}
    }
}
