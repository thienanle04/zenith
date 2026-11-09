package com.zenith.app.features.tabs

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.zenith.app.core.navigation.ZenithBottomBar

@Composable
fun BlankTab(
    title: String,
    current: NavKey,
    onSelectTab: (NavKey) -> Unit,
) {
    Scaffold(bottomBar = { ZenithBottomBar(current, onSelectTab) }) { padding ->
        Text(
            title,
            modifier = Modifier.padding(padding).padding(16.dp),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
