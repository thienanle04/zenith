package com.zenith.app.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TransactionsDestination : NavKey

@Serializable
data object StatsDestination : NavKey

@Serializable
data object PortfolioDestination : NavKey

@Serializable
data object MoreDestination : NavKey

@Serializable
data object DeleteDestination : NavKey

@Serializable
data object OrderDestination : NavKey

@Serializable
data object NewAccountDestination : NavKey

@Serializable
data class AccountDestination(
    val id: String,
) : NavKey

@Serializable
data object NewHoldingDestination : NavKey

@Serializable
data class HoldingDestination(
    val id: String,
) : NavKey
