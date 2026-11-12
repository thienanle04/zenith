package com.zenith.app.features.account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.zenith.app.core.l10n.accountKindLabel
import com.zenith.app.core.l10n.portfolioFailureMessage
import com.zenith.app.core.preview.SamplePortfolio
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing
import com.zenith.app.core.utils.formatMajor
import com.zenith.app.core.utils.moneyPrefix
import com.zenith.app.core.utils.moneySuffix
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.domain.portfolio.AccountEditor
import com.zenith.domain.portfolio.AccountKind
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioFailure
import com.zenith.domain.portfolio.PortfolioOutcome
import com.zenith.domain.portfolio.PortfolioView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: PortfolioViewModel,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(AccountKind.cash) }
    var currency by remember { mutableStateOf("VND") }
    var error by remember { mutableStateOf<PortfolioFailure?>(null) }
    val scope = rememberCoroutineScope()
    AddAccountScreen(
        view = portfolio,
        name = name,
        kind = kind,
        currency = currency,
        error = error,
        onName = { name = it },
        onKind = { kind = it },
        onCurrency = { currency = it },
        onDone = onDone,
        onSave = {
            scope.launch {
                error = viewModel.commit(PortfolioCommand.AddAccount(name, kind, currency))
                if (error == null) onDone()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountScreen(
    view: PortfolioView?,
    name: String,
    kind: AccountKind,
    currency: String,
    error: PortfolioFailure?,
    onName: (String) -> Unit,
    onKind: (AccountKind) -> Unit,
    onCurrency: (String) -> Unit,
    onDone: () -> Unit,
    onSave: () -> Unit,
) {
    EditorScaffold(stringResource(R.string.add_account), error, onDone, onSave = onSave) {
        AppTextRow(stringResource(R.string.name), name, onValue = onName)
        AppChoiceRow(stringResource(R.string.kind), stringResource(accountKindLabel(kind)), AccountKind.entries, { stringResource(accountKindLabel(it)) }, onSelect = onKind)
        AppChoiceRow(stringResource(R.string.currency), currency, view?.currencies ?: listOf("VND"), { it }, onSelect = onCurrency)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: PortfolioViewModel,
    id: String,
    onDone: () -> Unit,
) {
    var editor by remember { mutableStateOf<AccountEditor?>(null) }
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(AccountKind.cash) }
    var currency by remember { mutableStateOf("VND") }
    var balance by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<PortfolioFailure?>(null) }
    var confirming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(id) {
        when (val loaded = viewModel.accountEditor(id)) {
            is PortfolioOutcome.Ready -> {
                val value = loaded.value
                editor = value
                name = value.name
                kind = value.kind
                currency = value.currency
                balance = formatMajor(value.balance, value.separators)
                rate = formatMajor(value.rate, value.separators)
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
    AccountScreen(
        editor = loaded,
        failure = error,
        name = name,
        kind = kind,
        currency = currency,
        balance = balance,
        rate = rate,
        confirming = confirming,
        onName = { name = it },
        onKind = { kind = it },
        onCurrency = { selected ->
            scope.launch {
                when (val preview = viewModel.currencyPreview(loaded.id, selected)) {
                    is PortfolioOutcome.Refused -> error = preview.failure
                    is PortfolioOutcome.Ready -> {
                        error = null
                        currency = selected
                        rate = formatMajor(preview.value.rate, loaded.separators)
                        balance = formatMajor(preview.value.balance, loaded.separators)
                    }
                }
            }
        },
        onBalance = { balance = it },
        onRate = { rate = it },
        onConfirming = { confirming = it },
        onDone = onDone,
        onSave = {
            scope.launch {
                error =
                    viewModel.commit(
                        PortfolioCommand.SaveAccount(
                            id = loaded.id,
                            name = name,
                            kind = kind,
                            currency = currency,
                            balanceText = balance,
                            rateText = if (currency == loaded.currency) null else rate,
                        ),
                    )
                if (error == null) onDone()
            }
        },
        onSetAside = {
            scope.launch {
                val code = viewModel.commit(PortfolioCommand.SetAside(id))
                if (code == null) onDone() else error = code
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountScreen(
    editor: AccountEditor,
    failure: PortfolioFailure?,
    name: String,
    kind: AccountKind,
    currency: String,
    balance: String,
    rate: String,
    confirming: Boolean,
    onName: (String) -> Unit,
    onKind: (AccountKind) -> Unit,
    onCurrency: (String) -> Unit,
    onBalance: (String) -> Unit,
    onRate: (String) -> Unit,
    onConfirming: (Boolean) -> Unit,
    onDone: () -> Unit,
    onSave: () -> Unit,
    onSetAside: () -> Unit,
) {
    val kinds = if (editor.kind == AccountKind.stock) listOf(AccountKind.stock) else AccountKind.entries.filter { it != AccountKind.stock }
    EditorScaffold(editor.name, failure, onDone, onSave = onSave) {
        AppTextRow(stringResource(R.string.name), name, onValue = onName)
        AppChoiceRow(
            stringResource(R.string.kind),
            stringResource(accountKindLabel(kind)),
            kinds,
            { stringResource(accountKindLabel(it)) },
            enabled = editor.kind != AccountKind.stock,
            onSelect = onKind,
        )
        AppChoiceRow(stringResource(R.string.currency), currency, editor.currencies, { it }, onSelect = onCurrency)
        if (currency != editor.currency) {
            AppTextRow(
                stringResource(R.string.rate),
                rate,
                KeyboardType.Decimal,
                separators = editor.separators,
                suffix = moneySuffix("VND"),
                onValue = onRate,
            )
        }
        AppTextRow(
            stringResource(R.string.balance),
            balance,
            KeyboardType.Decimal,
            separators = editor.separators,
            prefix = moneyPrefix(currency),
            suffix = moneySuffix(currency),
            onValue = onBalance,
        )
        Button(onClick = { onConfirming(true) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.set_aside))
        }
        if (confirming) {
            val accountName = name.ifBlank { editor.name }
            ConfirmDialog(
                message =
                    stringResource(
                        if (editor.kind == AccountKind.stock) R.string.set_aside_shares_confirm else R.string.set_aside_confirm,
                        accountName,
                    ),
                confirm = stringResource(R.string.set_aside),
                onConfirm = {
                    onConfirming(false)
                    onSetAside()
                },
                onDismiss = { onConfirming(false) },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddAccountScreenPreview() {
    ZenithTheme {
        AddAccountScreen(
            view = SamplePortfolio.view,
            name = "",
            kind = AccountKind.cash,
            currency = "VND",
            error = null,
            onName = {},
            onKind = {},
            onCurrency = {},
            onDone = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AccountScreenPreview() {
    val editor = SamplePortfolio.bankAccount
    ZenithTheme {
        AccountScreen(
            editor = editor,
            failure = null,
            name = editor.name,
            kind = editor.kind,
            currency = editor.currency,
            balance = formatMajor(editor.balance, editor.separators),
            rate = formatMajor(editor.rate, editor.separators),
            confirming = false,
            onName = {},
            onKind = {},
            onCurrency = {},
            onBalance = {},
            onRate = {},
            onConfirming = {},
            onDone = {},
            onSave = {},
            onSetAside = {},
        )
    }
}
