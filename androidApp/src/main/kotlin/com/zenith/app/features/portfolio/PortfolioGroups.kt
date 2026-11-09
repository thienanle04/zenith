package com.zenith.app.features.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zenith.app.R
import com.zenith.app.core.theme.NegativeAmount
import com.zenith.app.core.theme.PositiveAmount
import com.zenith.app.core.utils.formatMinor
import com.zenith.app.core.utils.formatMoney
import com.zenith.app.core.utils.formatQuantity
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.domain.money.Exact
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioRow
import com.zenith.domain.portfolio.PortfolioSection
import com.zenith.domain.portfolio.PortfolioView
import com.zenith.domain.portfolio.RowKind
import com.zenith.domain.portfolio.SectionKind
import kotlinx.coroutines.launch

@Composable
fun PortfolioGroups(
    view: PortfolioView,
    viewModel: PortfolioViewModel,
    modifier: Modifier = Modifier,
    reorder: Boolean = false,
    onRow: ((PortfolioRow) -> Unit)? = null,
) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(view.sections, key = { _, section -> section.key }) { index, section ->
            SectionBlock(
                view = view,
                section = section,
                index = index,
                count = view.sections.size,
                viewModel = viewModel,
                reorder = reorder,
                onRow = onRow,
            )
        }
    }
}

@Composable
private fun SectionBlock(
    view: PortfolioView,
    section: PortfolioSection,
    index: Int,
    count: Int,
    viewModel: PortfolioViewModel,
    reorder: Boolean,
    onRow: ((PortfolioRow) -> Unit)?,
) {
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(sectionTitle(section), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            val quantity = section.subtotalQuantity
            if (quantity != null) {
                Text(formatQuantity(quantity, section.subtotalUnit ?: "", view.separators))
            } else {
                val minor = section.subtotalMinor ?: 0L
                Text(formatMinor(kotlin.math.abs(minor), "VND", view.separators), color = amountColor(minor))
            }
            if (reorder) {
                MoveButtons(
                    upEnabled = index > 0,
                    downEnabled = index < count - 1,
                    onUp = { scope.launch { viewModel.commit(PortfolioCommand.MoveSection(section.key, -1)) } },
                    onDown = { scope.launch { viewModel.commit(PortfolioCommand.MoveSection(section.key, 1)) } },
                )
            }
        }
        HorizontalDivider(thickness = .3.dp)
        section.rows.forEachIndexed { rowIndex, row ->
            SectionRow(
                view = view,
                section = section,
                row = row,
                reorder = reorder,
                upEnabled = rowIndex > 0,
                downEnabled = rowIndex < section.rows.lastIndex,
                onRow = onRow,
                onUp = { scope.launch { viewModel.commit(PortfolioCommand.MoveRow(section.key, row.id, -1)) } },
                onDown = { scope.launch { viewModel.commit(PortfolioCommand.MoveRow(section.key, row.id, 1)) } },
            )
            HorizontalDivider(thickness = .3.dp)
        }
    }
}

@Composable
private fun SectionRow(
    view: PortfolioView,
    section: PortfolioSection,
    row: PortfolioRow,
    reorder: Boolean,
    upEnabled: Boolean,
    downEnabled: Boolean,
    onRow: ((PortfolioRow) -> Unit)?,
    onUp: () -> Unit,
    onDown: () -> Unit,
) {
    val shares = section.kind == SectionKind.shares
    val amount = rowAmount(row, view, shares)
    val sign =
        when {
            shares -> 0
            row.kind == RowKind.account -> (row.minor ?: 0L).sign()
            else -> (row.value ?: Exact.zero).sign
        }
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onRow == null) Modifier else Modifier.clickable { onRow(row) })
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.title, style = MaterialTheme.typography.bodyMedium)
            if (row.kind == RowKind.holding && section.kind != SectionKind.shares) {
                Text(
                    formatQuantity(row.quantity ?: Exact.zero, row.unit ?: "", view.separators),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF737373),
                )
            }
        }
        Text(amount, color = amountColor(sign.toLong()), style = MaterialTheme.typography.bodyMedium)
        if (reorder) {
            MoveButtons(upEnabled, downEnabled, onUp, onDown)
        }
    }
}

@Composable
private fun MoveButtons(
    upEnabled: Boolean,
    downEnabled: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
) {
    IconButton(onClick = onUp, enabled = upEnabled) { Text(stringResource(R.string.move_up)) }
    IconButton(onClick = onDown, enabled = downEnabled) { Text(stringResource(R.string.move_down)) }
}

@Composable
private fun sectionTitle(section: PortfolioSection): String =
    when (section.kind) {
        SectionKind.cash -> stringResource(R.string.cash)
        SectionKind.bank -> stringResource(R.string.bank)
        SectionKind.eWallet -> stringResource(R.string.e_wallet)
        SectionKind.creditCard -> stringResource(R.string.credit_card)
        SectionKind.loan -> stringResource(R.string.loan)
        SectionKind.receivable -> stringResource(R.string.receivable)
        SectionKind.stock -> stringResource(R.string.stock)
        SectionKind.physical -> stringResource(R.string.physical_holdings)
        SectionKind.shares -> section.heading ?: stringResource(R.string.shares)
    }

private fun rowAmount(
    row: PortfolioRow,
    view: PortfolioView,
    shares: Boolean,
): String =
    when {
        shares -> formatQuantity(row.quantity ?: Exact.zero, row.unit ?: "", view.separators)
        row.kind == RowKind.account -> formatMinor(row.minor ?: 0L, row.currency ?: "VND", view.separators)
        else -> formatMoney(row.value ?: Exact.zero, row.valueCurrency ?: "VND", view.separators)
    }

internal fun amountColor(amount: Long): Color =
    when {
        amount > 0L -> PositiveAmount
        amount < 0L -> NegativeAmount
        else -> Color(0xFF0A0A0A)
    }

private fun Long.sign(): Int =
    when {
        this > 0L -> 1
        this < 0L -> -1
        else -> 0
    }
