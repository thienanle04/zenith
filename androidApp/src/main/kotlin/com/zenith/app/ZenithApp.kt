package com.zenith.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.zenith.app.core.navigation.AboutZenithDestination
import com.zenith.app.core.navigation.AccountDestination
import com.zenith.app.core.navigation.AmountPrecisionDestination
import com.zenith.app.core.navigation.DeleteDestination
import com.zenith.app.core.navigation.ExchangeRatesDestination
import com.zenith.app.core.navigation.GeneralSettingsDestination
import com.zenith.app.core.navigation.GuideFaqDestination
import com.zenith.app.core.navigation.HoldingDestination
import com.zenith.app.core.navigation.LocalizationFormattingDestination
import com.zenith.app.core.navigation.MoreDestination
import com.zenith.app.core.navigation.NewAccountDestination
import com.zenith.app.core.navigation.NewHoldingDestination
import com.zenith.app.core.navigation.OrderDestination
import com.zenith.app.core.navigation.PortfolioDestination
import com.zenith.app.core.navigation.StatsDestination
import com.zenith.app.core.navigation.ThemeStyleDestination
import com.zenith.app.core.navigation.TransactionsDestination
import com.zenith.app.core.navigation.immediateTransition
import com.zenith.app.core.navigation.slideMetadata
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.app.features.account.AccountScreen
import com.zenith.app.features.account.AddAccountScreen
import com.zenith.app.features.holding.AddHoldingScreen
import com.zenith.app.features.holding.HoldingScreen
import com.zenith.app.features.more.AmountPrecisionScreen
import com.zenith.app.features.more.ExchangeRatesScreen
import com.zenith.app.features.more.LocalizationScreen
import com.zenith.app.features.more.MoreBlankScreen
import com.zenith.app.features.more.MoreScreen
import com.zenith.app.features.portfolio.DeleteScreen
import com.zenith.app.features.portfolio.OrderScreen
import com.zenith.app.features.portfolio.PortfolioScreen
import com.zenith.app.features.tabs.BlankTab
import com.zenith.app.features.transactions.TransactionsScreen
import com.zenith.domain.portfolio.PortfolioRepository

@Composable
fun ZenithApp(repository: PortfolioRepository) {
    val viewModel: PortfolioViewModel = viewModel(factory = PortfolioViewModel.factory(repository))
    val backStack = rememberNavBackStack(PortfolioDestination)
    val selectTab: (NavKey) -> Unit = { destination ->
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
            backStack.removeAt(0)
        }
    }
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = { immediateTransition },
        popTransitionSpec = { immediateTransition },
        predictivePopTransitionSpec = { immediateTransition },
        entryProvider =
            entryProvider {
                entry<TransactionsDestination> {
                    TransactionsScreen(viewModel, selectTab)
                }
                entry<StatsDestination> {
                    BlankTab(stringResource(R.string.stats), StatsDestination, selectTab)
                }
                entry<PortfolioDestination> {
                    PortfolioScreen(
                        viewModel = viewModel,
                        onAddAccount = { backStack.add(NewAccountDestination) },
                        onAddHolding = { backStack.add(NewHoldingDestination) },
                        onOpenAccount = { backStack.add(AccountDestination(it)) },
                        onOpenHolding = { backStack.add(HoldingDestination(it)) },
                        onDelete = { backStack.add(DeleteDestination) },
                        onModifyOrders = { backStack.add(OrderDestination) },
                        onSelectTab = selectTab,
                    )
                }
                entry<MoreDestination> {
                    MoreScreen(onOpen = { backStack.add(it) }, onSelectTab = selectTab)
                }
                entry<GeneralSettingsDestination>(metadata = slideMetadata) {
                    MoreBlankScreen(stringResource(R.string.general_settings)) { backStack.removeLastOrNull() }
                }
                entry<ThemeStyleDestination>(metadata = slideMetadata) {
                    MoreBlankScreen(stringResource(R.string.theme_style)) { backStack.removeLastOrNull() }
                }
                entry<LocalizationFormattingDestination>(metadata = slideMetadata) {
                    LocalizationScreen(
                        viewModel,
                        onOpenRates = { backStack.add(ExchangeRatesDestination) },
                        onOpenPrecision = { backStack.add(AmountPrecisionDestination) },
                        onDone = { backStack.removeLastOrNull() },
                    )
                }
                entry<ExchangeRatesDestination>(metadata = slideMetadata) {
                    ExchangeRatesScreen(viewModel) { backStack.removeLastOrNull() }
                }
                entry<AmountPrecisionDestination>(metadata = slideMetadata) {
                    AmountPrecisionScreen(viewModel) { backStack.removeLastOrNull() }
                }
                entry<GuideFaqDestination>(metadata = slideMetadata) {
                    MoreBlankScreen(stringResource(R.string.guide_faq)) { backStack.removeLastOrNull() }
                }
                entry<AboutZenithDestination>(metadata = slideMetadata) {
                    MoreBlankScreen(stringResource(R.string.about_zenith)) { backStack.removeLastOrNull() }
                }
                entry<DeleteDestination>(metadata = slideMetadata) {
                    DeleteScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<OrderDestination>(metadata = slideMetadata) {
                    OrderScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<NewAccountDestination>(metadata = slideMetadata) {
                    AddAccountScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<AccountDestination>(metadata = slideMetadata) { key ->
                    AccountScreen(viewModel, key.id, onDone = { backStack.removeLastOrNull() })
                }
                entry<NewHoldingDestination>(metadata = slideMetadata) {
                    AddHoldingScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<HoldingDestination>(metadata = slideMetadata) { key ->
                    HoldingScreen(viewModel, key.id, onDone = { backStack.removeLastOrNull() })
                }
            },
    )
}
