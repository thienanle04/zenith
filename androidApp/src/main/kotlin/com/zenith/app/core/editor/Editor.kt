package com.zenith.app.core.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zenith.app.R
import com.zenith.app.core.l10n.portfolioFailureMessage
import com.zenith.app.core.theme.spacing
import com.zenith.domain.portfolio.PortfolioFailure

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackScaffold(
    title: String,
    onDone: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(
                    onClick = onDone,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            colors = editorTopBarColors(),
        )
    }) { padding ->
        content(padding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScaffold(
    title: String,
    error: PortfolioFailure?,
    onDone: () -> Unit,
    onSave: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                TextButton(
                    onClick = onDone,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                ) {
                    Text("‹")
                }
            },
            colors = editorTopBarColors(),
        )
    }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.mediumSmall),
        ) {
            content()
            if (error != null) Text(stringResource(portfolioFailureMessage(error)))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun editorTopBarColors() =
    TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    )
