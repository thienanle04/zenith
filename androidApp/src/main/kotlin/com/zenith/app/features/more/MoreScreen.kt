package com.zenith.app.features.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.zenith.app.R
import com.zenith.app.core.editor.BackScaffold
import com.zenith.app.core.navigation.AboutZenithDestination
import com.zenith.app.core.navigation.GeneralSettingsDestination
import com.zenith.app.core.navigation.GuideFaqDestination
import com.zenith.app.core.navigation.LocalizationFormattingDestination
import com.zenith.app.core.navigation.MoreDestination
import com.zenith.app.core.navigation.ThemeStyleDestination
import com.zenith.app.core.navigation.ZenithBottomBar
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing
import com.zenith.app.features.more.components.MoreTopBar

private data class MoreEntry(
    val destination: NavKey,
    val icon: ImageVector,
    val title: Int,
    val subtitle: Int?,
)

private val entries =
    listOf(
        MoreEntry(GeneralSettingsDestination, Icons.Outlined.Settings, R.string.general_settings, R.string.general_settings_detail),
        MoreEntry(ThemeStyleDestination, Icons.Outlined.Palette, R.string.theme_style, R.string.theme_style_detail),
        MoreEntry(
            LocalizationFormattingDestination,
            Icons.Outlined.Translate,
            R.string.localization_formatting,
            R.string.localization_formatting_detail,
        ),
        MoreEntry(GuideFaqDestination, Icons.Outlined.RateReview, R.string.feedback, null),
        MoreEntry(GuideFaqDestination, Icons.AutoMirrored.Outlined.HelpOutline, R.string.guide_faq, null),
        MoreEntry(AboutZenithDestination, Icons.Outlined.Info, R.string.about_zenith, null),
    )

@Composable
fun MoreScreen(
    onOpen: (NavKey) -> Unit,
    onSelectTab: (NavKey) -> Unit,
) {
    Scaffold(
        topBar = { MoreTopBar() },
        bottomBar = { ZenithBottomBar(MoreDestination, onSelectTab) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surfaceContainer).padding(MaterialTheme.spacing.medium)) {
            entries.forEach { option ->
                MoreRow(option, onOpen)
            }
        }
    }
}

@Composable
private fun MoreRow(
    option: MoreEntry,
    onOpen: (NavKey) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onOpen(option.destination) }
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        Icon(option.icon, contentDescription = null)
        Column(Modifier.weight(1f)) {
            Text(stringResource(option.title), style = MaterialTheme.typography.bodyLarge)
            option.subtitle?.let {
                Text(stringResource(it),  style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MoreBlankScreen(
    title: String,
    onDone: () -> Unit,
) {
    BackScaffold(title, onDone) { _ -> }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MoreScreenPreview() {
    ZenithTheme {
        MoreScreen(onOpen = {}, onSelectTab = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsPreview() {
    ZenithTheme {
        MoreBlankScreen(stringResource(R.string.general_settings)) {}
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ThemeStylePreview() {
    ZenithTheme {
        MoreBlankScreen(stringResource(R.string.theme_style)) {}
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GuideFaqPreview() {
    ZenithTheme {
        MoreBlankScreen(stringResource(R.string.guide_faq)) {}
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AboutZenithPreview() {
    ZenithTheme {
        MoreBlankScreen(stringResource(R.string.about_zenith)) {}
    }
}
