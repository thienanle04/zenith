package com.zenith.app.features.portfolio

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith.app.R
import com.zenith.app.core.components.ConfirmDialog
import com.zenith.app.core.editor.BackScaffold
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioRow
import com.zenith.domain.portfolio.RowKind
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteScreen(
    viewModel: PortfolioViewModel,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf<PortfolioRow?>(null) }
    val scope = rememberCoroutineScope()
    BackScaffold(stringResource(R.string.delete), onDone) { padding ->
        val view = portfolio
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
            return@BackScaffold
        }
        PortfolioGroups(
            view = view,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            onRow = { pending = it },
        )
        val row = pending
        if (row != null) {
            val account = row.kind == RowKind.account
            ConfirmDialog(
                message =
                    stringResource(
                        when {
                            account && row.stock -> R.string.set_aside_shares_confirm
                            account -> R.string.set_aside_confirm
                            else -> R.string.remove_holding_confirm
                        },
                        row.title,
                    ),
                confirm = stringResource(if (account) R.string.set_aside else R.string.remove_holding),
                onConfirm = {
                    val command =
                        if (account) {
                            PortfolioCommand.SetAside(row.id)
                        } else {
                            PortfolioCommand.RemoveHolding(row.id)
                        }
                    pending = null
                    scope.launch { viewModel.commit(command) }
                },
                onDismiss = { pending = null },
            )
        }
    }
}
