package com.zenith.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.zenith.app.R
import com.zenith.app.core.navigation.AccountDestination
import com.zenith.app.core.navigation.DeleteDestination
import com.zenith.app.core.navigation.HoldingDestination
import com.zenith.app.core.navigation.MoreDestination
import com.zenith.app.core.navigation.NewAccountDestination
import com.zenith.app.core.navigation.NewHoldingDestination
import com.zenith.app.core.navigation.OrderDestination
import com.zenith.app.core.navigation.PortfolioDestination
import com.zenith.app.core.navigation.StatsDestination
import com.zenith.app.core.navigation.TransactionsDestination
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.app.features.account.AccountScreen
import com.zenith.app.features.account.AddAccountScreen
import com.zenith.app.features.holding.AddHoldingScreen
import com.zenith.app.features.holding.HoldingScreen
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
                    BlankTab(stringResource(R.string.more), MoreDestination, selectTab)
                }
                entry<DeleteDestination> {
                    DeleteScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<OrderDestination> {
                    OrderScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<NewAccountDestination> {
                    AddAccountScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<AccountDestination> { key ->
                    AccountScreen(viewModel, key.id, onDone = { backStack.removeLastOrNull() })
                }
                entry<NewHoldingDestination> {
                    AddHoldingScreen(viewModel, onDone = { backStack.removeLastOrNull() })
                }
                entry<HoldingDestination> { key ->
                    HoldingScreen(viewModel, key.id, onDone = { backStack.removeLastOrNull() })
                }
            },
    )
}
