package com.zenith.app.features.holding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith.app.R
import com.zenith.app.core.components.AppChoiceRow
import com.zenith.app.core.components.AppTextRow
import com.zenith.app.core.components.ConfirmDialog
import com.zenith.app.core.editor.EditorScaffold
import com.zenith.app.core.l10n.portfolioFailureMessage
import com.zenith.app.core.preview.SamplePortfolio
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing
import com.zenith.app.core.utils.formatMajor
import com.zenith.app.core.utils.moneyPrefix
import com.zenith.app.core.utils.moneySuffix
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.domain.portfolio.HoldingEditor
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioFailure
import com.zenith.domain.portfolio.PortfolioOutcome
import com.zenith.domain.portfolio.PortfolioView
import com.zenith.domain.portfolio.SeparatorPair
import com.zenith.domain.portfolio.StockAccountChoice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingScreen(
    viewModel: PortfolioViewModel,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("0") }
    var currency by remember { mutableStateOf("VND") }
    var shares by remember { mutableStateOf(false) }
    var stockAccountId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<PortfolioFailure?>(null) }
    val scope = rememberCoroutineScope()
    val view = portfolio
    val accounts = view?.stockAccounts.orEmpty()
    LaunchedEffect(accounts) {
        if (stockAccountId == null) stockAccountId = accounts.firstOrNull()?.id
        if (accounts.isEmpty()) shares = false
    }
    AddHoldingScreen(
        view = view,
        name = name,
        unit = unit,
        price = price,
        quantity = quantity,
        currency = currency,
        shares = shares,
        stockAccountId = stockAccountId,
        error = error,
        onName = { name = it },
        onUnit = { unit = it },
        onPrice = { price = it },
        onQuantity = { quantity = it },
        onCurrency = { currency = it },
        onShares = { on ->
            shares = on && accounts.isNotEmpty()
            if (shares && stockAccountId == null) stockAccountId = accounts.first().id
        },
        onAccount = { stockAccountId = it },
        onDone = onDone,
        onSave = {
            scope.launch {
                val command = addHoldingCommand(shares, stockAccountId, name, unit, currency, price, quantity) ?: return@launch
                error = viewModel.commit(command)
                if (error == null) onDone()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHoldingScreen(
    view: PortfolioView?,
    name: String,
    unit: String,
    price: String,
    quantity: String,
    currency: String,
    shares: Boolean,
    stockAccountId: String?,
    error: PortfolioFailure?,
    onName: (String) -> Unit,
    onUnit: (String) -> Unit,
    onPrice: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onCurrency: (String) -> Unit,
    onShares: (Boolean) -> Unit,
    onAccount: (String) -> Unit,
    onDone: () -> Unit,
    onSave: () -> Unit,
) {
    val accounts = view?.stockAccounts.orEmpty()
    val separators = view?.separators ?: SeparatorPair.commaPoint
    EditorScaffold(stringResource(R.string.add_holding), error, onDone, onSave = onSave) {
        AppTextRow(stringResource(R.string.name), name, onValue = onName)
        AppTextRow(stringResource(R.string.unit), unit, onValue = onUnit)
        AppTextRow(
            stringResource(R.string.price),
            price,
            KeyboardType.Decimal,
            separators = separators,
            prefix = moneyPrefix(currency),
            suffix = moneySuffix(currency),
            onValue = onPrice,
        )
        AppTextRow(
            stringResource(R.string.quantity),
            quantity,
            KeyboardType.Decimal,
            separators = separators,
            suffix = if (unit.isBlank()) "" else " ${unit.trim()}",
            onValue = onQuantity,
        )
        AppChoiceRow(stringResource(R.string.currency), currency, view?.currencies ?: listOf("VND"), { it }, onSelect = onCurrency)
        ShareFields(
            shares = shares,
            accounts = accounts,
            stockAccountId = stockAccountId,
            onShares = onShares,
            onAccount = onAccount,
        )
    }
}

@Composable
private fun ShareFields(
    shares: Boolean,
    accounts: List<StockAccountChoice>,
    stockAccountId: String?,
    onShares: (Boolean) -> Unit,
    onAccount: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.shares), modifier = Modifier.weight(1f))
        Switch(
            checked = shares,
            enabled = accounts.isNotEmpty(),
            onCheckedChange = onShares,
        )
    }
    if (shares && accounts.isNotEmpty()) {
        val selected = accounts.firstOrNull { it.id == stockAccountId }?.name ?: accounts.first().name
        AppChoiceRow(stringResource(R.string.stock), selected, accounts, { it.name }) { onAccount(it.id) }
    }
}

private fun addHoldingCommand(
    shares: Boolean,
    stockAccountId: String?,
    name: String,
    unit: String,
    currency: String,
    price: String,
    quantity: String,
): PortfolioCommand? {
    val accountId = stockAccountId
    if (shares && accountId == null) return null
    return if (shares && accountId != null) {
        PortfolioCommand.AddShares(name, unit, currency, price, quantity, accountId)
    } else {
        PortfolioCommand.AddPhysicalHolding(name, unit, currency, price, quantity)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingScreen(
    viewModel: PortfolioViewModel,
    id: String,
    onDone: () -> Unit,
) {
    var editor by remember { mutableStateOf<HoldingEditor?>(null) }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<PortfolioFailure?>(null) }
    var confirmingHolding by remember { mutableStateOf(false) }
    var confirmingInstrument by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(id) {
        when (val loaded = viewModel.holdingEditor(id)) {
            is PortfolioOutcome.Ready -> {
                val value = loaded.value
                editor = value
                name = value.name
                price = formatMajor(value.price, value.separators)
                quantity = formatMajor(value.quantity, value.separators)
            }
            is PortfolioOutcome.Refused -> error = loaded.failure
        }
    }
    val loaded = editor
    val failure = error
    if (loaded == null) {
        if (failure == null) {
            CircularProgressIndicator(Modifier.padding(MaterialTheme.spacing.large))
        } else {
            Text(stringResource(portfolioFailureMessage(failure)), modifier = Modifier.padding(MaterialTheme.spacing.large))
        }
        return
    }
    HoldingScreen(
        editor = loaded,
        failure = error,
        name = name,
        price = price,
        quantity = quantity,
        confirmingHolding = confirmingHolding,
        confirmingInstrument = confirmingInstrument,
        onName = { name = it },
        onPrice = { price = it },
        onQuantity = { quantity = it },
        onConfirmHolding = { confirmingHolding = it },
        onConfirmInstrument = { confirmingInstrument = it },
        onDone = onDone,
        onSave = {
            scope.launch {
                error = viewModel.commit(PortfolioCommand.SaveHolding(loaded.holdingId, name, price, quantity))
                if (error == null) onDone()
            }
        },
        onRemoveHolding = {
            scope.launch {
                val code = viewModel.commit(PortfolioCommand.RemoveHolding(id))
                if (code == null) onDone() else error = code
            }
        },
        onRemoveInstrument = {
            scope.launch {
                val code = viewModel.commit(PortfolioCommand.RemoveInstrument(loaded.instrumentId))
                if (code == null) onDone() else error = code
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingScreen(
    editor: HoldingEditor,
    failure: PortfolioFailure?,
    name: String,
    price: String,
    quantity: String,
    confirmingHolding: Boolean,
    confirmingInstrument: Boolean,
    onName: (String) -> Unit,
    onPrice: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onConfirmHolding: (Boolean) -> Unit,
    onConfirmInstrument: (Boolean) -> Unit,
    onDone: () -> Unit,
    onSave: () -> Unit,
    onRemoveHolding: () -> Unit,
    onRemoveInstrument: () -> Unit,
) {
    EditorScaffold(editor.name, failure, onDone, onSave = onSave) {
        AppTextRow(stringResource(R.string.name), name, onValue = onName)
        Text(stringResource(R.string.unit) + ": " + editor.unit)
        Text(stringResource(R.string.currency) + ": " + editor.priceCurrency)
        AppTextRow(
            stringResource(R.string.price),
            price,
            KeyboardType.Decimal,
            separators = editor.separators,
            prefix = moneyPrefix(editor.priceCurrency),
            suffix = moneySuffix(editor.priceCurrency),
            onValue = onPrice,
        )
        AppTextRow(
            stringResource(R.string.quantity),
            quantity,
            KeyboardType.Decimal,
            separators = editor.separators,
            suffix = " ${editor.unit}",
            onValue = onQuantity,
        )
        Button(onClick = { onConfirmHolding(true) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.remove_holding))
        }
        Button(onClick = { onConfirmInstrument(true) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.remove_instrument))
        }
        if (confirmingHolding) {
            ConfirmDialog(
                message = stringResource(R.string.remove_holding_confirm, name.ifBlank { editor.name }),
                confirm = stringResource(R.string.remove_holding),
                onConfirm = {
                    onConfirmHolding(false)
                    onRemoveHolding()
                },
                onDismiss = { onConfirmHolding(false) },
            )
        }
        if (confirmingInstrument) {
            ConfirmDialog(
                message = stringResource(R.string.remove_instrument_confirm, name.ifBlank { editor.name }),
                confirm = stringResource(R.string.remove_instrument),
                onConfirm = {
                    onConfirmInstrument(false)
                    onRemoveInstrument()
                },
                onDismiss = { onConfirmInstrument(false) },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddHoldingScreenPreview() {
    ZenithTheme {
        AddHoldingScreen(
            view = SamplePortfolio.view,
            name = "",
            unit = "",
            price = "",
            quantity = "0",
            currency = "VND",
            shares = false,
            stockAccountId = null,
            error = null,
            onName = {},
            onUnit = {},
            onPrice = {},
            onQuantity = {},
            onCurrency = {},
            onShares = {},
            onAccount = {},
            onDone = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HoldingScreenPreview() {
    val editor = SamplePortfolio.holding
    ZenithTheme {
        HoldingScreen(
            editor = editor,
            failure = null,
            name = editor.name,
            price = formatMajor(editor.price, editor.separators),
            quantity = formatMajor(editor.quantity, editor.separators),
            confirmingHolding = false,
            confirmingInstrument = false,
            onName = {},
            onPrice = {},
            onQuantity = {},
            onConfirmHolding = {},
            onConfirmInstrument = {},
            onDone = {},
            onSave = {},
            onRemoveHolding = {},
            onRemoveInstrument = {},
        )
    }
}
