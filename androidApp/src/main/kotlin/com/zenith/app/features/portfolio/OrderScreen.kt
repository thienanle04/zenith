package com.zenith.app.features.portfolio

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith.app.R
import com.zenith.app.core.editor.BackScaffold
import com.zenith.app.core.writer.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    viewModel: PortfolioViewModel,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    BackScaffold(stringResource(R.string.modify_orders), onDone) { padding ->
        val view = portfolio
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
            return@BackScaffold
        }
        PortfolioGroups(
            view = view,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            reorder = true,
        )
    }
}
