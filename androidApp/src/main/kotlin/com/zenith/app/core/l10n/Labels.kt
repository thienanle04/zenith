package com.zenith.app.core.l10n

import androidx.annotation.StringRes
import com.zenith.app.R
import com.zenith.domain.portfolio.AccountKind
import com.zenith.domain.portfolio.PortfolioFailure

@StringRes
fun accountKindLabel(kind: AccountKind): Int =
    when (kind) {
        AccountKind.cash -> R.string.cash
        AccountKind.bank -> R.string.bank
        AccountKind.eWallet -> R.string.e_wallet
        AccountKind.creditCard -> R.string.credit_card
        AccountKind.loan -> R.string.loan
        AccountKind.receivable -> R.string.receivable
        AccountKind.stock -> R.string.stock
    }

@StringRes
fun portfolioFailureMessage(failure: PortfolioFailure): Int =
    when (failure) {
        PortfolioFailure.NameRequired -> R.string.name_required
        PortfolioFailure.CurrencyNeedsRate -> R.string.currency_needs_rate
        PortfolioFailure.StockKindFixed -> R.string.stock_kind_fixed
        PortfolioFailure.StockAccountRequired -> R.string.stock_account_required
        PortfolioFailure.RatePositive -> R.string.rate_positive
        PortfolioFailure.NotFound -> R.string.not_found
        PortfolioFailure.InvalidAmount -> R.string.invalid_amount
    }
