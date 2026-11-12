package com.zenith.app.features.tabs

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import com.zenith.app.R
import com.zenith.app.core.navigation.StatsDestination
import com.zenith.app.core.navigation.ZenithBottomBar
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing

@Composable
fun BlankTab(
    title: String,
    current: NavKey,
    onSelectTab: (NavKey) -> Unit,
) {
    Scaffold(bottomBar = { ZenithBottomBar(current, onSelectTab) }) { padding ->
        Text(
            title,
            modifier = Modifier.padding(padding).padding(MaterialTheme.spacing.medium),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StatsTabPreview() {
    ZenithTheme {
        BlankTab(stringResource(R.string.stats), StatsDestination) {}
    }
}
