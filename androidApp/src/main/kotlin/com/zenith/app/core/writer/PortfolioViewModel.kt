package com.zenith.app.core.writer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenith.domain.portfolio.AccountEditor
import com.zenith.domain.portfolio.CurrencyPreview
import com.zenith.domain.portfolio.HoldingEditor
import com.zenith.domain.portfolio.PortfolioBook
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioFailure
import com.zenith.domain.portfolio.PortfolioOutcome
import com.zenith.domain.portfolio.PortfolioRepository
import com.zenith.domain.portfolio.PortfolioView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PortfolioViewModel(
    private val repository: PortfolioRepository,
) : ViewModel() {
    private val portfolioState = MutableStateFlow<PortfolioView?>(null)
    private val saves = Mutex()
    val portfolio: StateFlow<PortfolioView?> = portfolioState.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    suspend fun commit(command: PortfolioCommand): PortfolioFailure? =
        saves.withLock {
            when (val outcome = PortfolioBook.apply(repository.load(), command)) {
                is PortfolioOutcome.Ready -> {
                    repository.save(outcome.value)
                    portfolioState.value = PortfolioBook.present(outcome.value)
                    null
                }
                is PortfolioOutcome.Refused -> outcome.failure
            }
        }

    suspend fun accountEditor(id: String): PortfolioOutcome<AccountEditor> = PortfolioBook.accountEditor(repository.load(), id)

    suspend fun holdingEditor(id: String): PortfolioOutcome<HoldingEditor> = PortfolioBook.holdingEditor(repository.load(), id)

    suspend fun currencyPreview(
        id: String,
        currency: String,
    ): PortfolioOutcome<CurrencyPreview> = PortfolioBook.currencyPreview(repository.load(), id, currency)

    private suspend fun refresh() {
        portfolioState.value = PortfolioBook.present(repository.load())
    }

    companion object {
        fun factory(repository: PortfolioRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return PortfolioViewModel(repository) as T
                }
            }
    }
}
