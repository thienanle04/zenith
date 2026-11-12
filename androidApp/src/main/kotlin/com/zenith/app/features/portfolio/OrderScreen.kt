package com.zenith.app.features.portfolio

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith.app.R
import com.zenith.app.core.editor.BackScaffold
import com.zenith.app.core.preview.SamplePortfolio
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    viewModel: PortfolioViewModel,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    OrderScreen(
        view = portfolio,
        onDone = onDone,
        onMoveSection = { key, delta -> scope.launch { viewModel.commit(PortfolioCommand.MoveSection(key, delta)) } },
        onMoveRow = { sectionKey, rowId, delta ->
            scope.launch { viewModel.commit(PortfolioCommand.MoveRow(sectionKey, rowId, delta)) }
        },
    )
}

@Composable
private fun OrderScreen(
    view: PortfolioView?,
    onDone: () -> Unit,
    onMoveSection: (String, Int) -> Unit,
    onMoveRow: (String, String, Int) -> Unit,
) {
    BackScaffold(stringResource(R.string.modify_orders), onDone) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(MaterialTheme.spacing.large))
            return@BackScaffold
        }
        PortfolioGroups(
            view = view,
            modifier = Modifier.fillMaxSize().padding(padding).padding(MaterialTheme.spacing.medium),
            reorder = true,
            onMoveSection = onMoveSection,
            onMoveRow = onMoveRow,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrderScreenPreview() {
    ZenithTheme {
        OrderScreen(
            view = SamplePortfolio.view,
            onDone = {},
            onMoveSection = { _, _ -> },
            onMoveRow = { _, _, _ -> },
        )
    }
}
