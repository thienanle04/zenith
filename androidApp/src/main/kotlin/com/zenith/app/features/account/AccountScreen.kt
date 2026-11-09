package com.zenith.app.features.account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith.app.R
import com.zenith.app.core.components.AppChoiceRow
import com.zenith.app.core.components.AppTextRow
import com.zenith.app.core.components.ConfirmDialog
import com.zenith.app.core.editor.EditorScaffold
import com.zenith.app.core.l10n.accountKindLabel
import com.zenith.app.core.l10n.portfolioFailureMessage
import com.zenith.app.core.utils.formatMajor
import com.zenith.app.core.utils.moneyPrefix
import com.zenith.app.core.utils.moneySuffix
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.domain.portfolio.AccountEditor
import com.zenith.domain.portfolio.AccountKind
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioFailure
import com.zenith.domain.portfolio.PortfolioOutcome
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
    EditorScaffold(stringResource(R.string.add_account), error, onDone, onSave = {
        scope.launch {
            error = viewModel.commit(PortfolioCommand.AddAccount(name, kind, currency))
            if (error == null) onDone()
        }
    }) {
        AppTextRow(stringResource(R.string.name), name) { name = it }
        AppChoiceRow(stringResource(R.string.kind), stringResource(accountKindLabel(kind)), AccountKind.entries, { stringResource(accountKindLabel(it)) }) { kind = it }
        AppChoiceRow(stringResource(R.string.currency), currency, portfolio?.currencies ?: listOf("VND"), { it }) { currency = it }
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
            CircularProgressIndicator(Modifier.padding(24.dp))
        } else {
            Text(stringResource(portfolioFailureMessage(failure)), modifier = Modifier.padding(24.dp))
        }
        return
    }
    val kinds = if (loaded.kind == AccountKind.stock) listOf(AccountKind.stock) else AccountKind.entries.filter { it != AccountKind.stock }
    EditorScaffold(loaded.name, error, onDone, onSave = {
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
    }) {
        AppTextRow(stringResource(R.string.name), name) { name = it }
        AppChoiceRow(
            stringResource(R.string.kind),
            stringResource(accountKindLabel(kind)),
            kinds,
            { stringResource(accountKindLabel(it)) },
            enabled = loaded.kind != AccountKind.stock,
        ) { kind = it }
        AppChoiceRow(stringResource(R.string.currency), currency, loaded.currencies, { it }) { selected ->
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
        }
        if (currency != loaded.currency) {
            AppTextRow(
                stringResource(R.string.rate),
                rate,
                KeyboardType.Decimal,
                separators = loaded.separators,
                suffix = moneySuffix("VND"),
            ) { rate = it }
        }
        AppTextRow(
            stringResource(R.string.balance),
            balance,
            KeyboardType.Decimal,
            separators = loaded.separators,
            prefix = moneyPrefix(currency),
            suffix = moneySuffix(currency),
        ) { balance = it }
        Button(onClick = { confirming = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.set_aside))
        }
        if (confirming) {
            val accountName = name.ifBlank { loaded.name }
            ConfirmDialog(
                message =
                    stringResource(
                        if (loaded.kind == AccountKind.stock) R.string.set_aside_shares_confirm else R.string.set_aside_confirm,
                        accountName,
                    ),
                confirm = stringResource(R.string.set_aside),
                onConfirm = {
                    confirming = false
                    scope.launch {
                        val code = viewModel.commit(PortfolioCommand.SetAside(id))
                        if (code == null) onDone() else error = code
                    }
                },
                onDismiss = { confirming = false },
            )
        }
    }
}
