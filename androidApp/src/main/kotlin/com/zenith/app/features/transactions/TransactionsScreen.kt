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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.zenith.app.R
import com.zenith.app.core.navigation.TransactionsDestination
import com.zenith.app.core.navigation.ZenithBottomBar
import com.zenith.app.core.utils.formatMinor
import com.zenith.app.core.utils.formatQuantity
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.app.features.portfolio.amountColor
import com.zenith.domain.portfolio.SeparatorPair
import com.zenith.domain.portfolio.TransactionLine
import kotlin.math.abs

@Composable
fun TransactionsScreen(
    viewModel: PortfolioViewModel,
    onSelectTab: (NavKey) -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val view = portfolio
    Scaffold(bottomBar = { ZenithBottomBar(TransactionsDestination, onSelectTab) }) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.transactions), style = MaterialTheme.typography.headlineSmall)
            LazyColumn(Modifier.weight(1f)) {
                items(view.transactions, key = { it.id }) { line ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(line.title, modifier = Modifier.weight(1f))
                        Text(lineFigure(line, view.separators), color = amountColor(lineSign(line)))
                    }
                }
            }
        }
    }
}

private fun lineFigure(
    line: TransactionLine,
    separators: SeparatorPair,
): String {
    val quantity = line.quantity
    return if (quantity != null) {
        formatQuantity(quantity, line.unit ?: "", separators)
    } else {
        formatMinor(abs(line.minor ?: 0L), line.currency ?: "VND", separators)
    }
}

private fun lineSign(line: TransactionLine): Long {
    val quantity = line.quantity
    return if (quantity != null) quantity.sign.toLong() else (line.minor ?: 0L).sign()
}

private fun Long.sign(): Long =
    when {
        this > 0L -> 1L
        this < 0L -> -1L
        else -> 0L
    }
