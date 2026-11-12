@file:Suppress("TooManyFunctions")

package com.zenith.app.features.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith.app.R
import com.zenith.app.core.components.AppChoiceRow
import com.zenith.app.core.components.AppDividedColumn
import com.zenith.app.core.components.AppTextRow
import com.zenith.app.core.editor.BackScaffold
import com.zenith.app.core.l10n.applyStoredLanguage
import com.zenith.app.core.l10n.phoneUses24HourClock
import com.zenith.app.core.preview.SamplePortfolio
import com.zenith.app.core.theme.ZenithTheme
import com.zenith.app.core.theme.spacing
import com.zenith.app.core.writer.PortfolioViewModel
import com.zenith.domain.money.Exact
import com.zenith.domain.money.PRECISION_LIMIT
import com.zenith.domain.portfolio.AppLanguage
import com.zenith.domain.portfolio.CalendarDay
import com.zenith.domain.portfolio.ClockFormat
import com.zenith.domain.portfolio.DateFormat
import com.zenith.domain.portfolio.DateOrder
import com.zenith.domain.portfolio.DatePickerStyle
import com.zenith.domain.portfolio.FirstWeekday
import com.zenith.domain.portfolio.PortfolioCommand
import com.zenith.domain.portfolio.PortfolioView
import com.zenith.domain.portfolio.TimePickerStyle
import com.zenith.domain.portfolio.formatCalendarDay
import com.zenith.domain.portfolio.resolveDateOrder
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun LocalizationScreen(
    viewModel: PortfolioViewModel,
    onOpenRates: () -> Unit,
    onOpenPrecision: () -> Unit,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(portfolio?.language) {
        val language = portfolio?.language ?: return@LaunchedEffect
        context.applyStoredLanguage(language)
    }
    LocalizationScreen(
        view = portfolio,
        phoneIs24Hour = context.phoneUses24HourClock(),
        onSave = { command -> scope.launch { viewModel.commit(command) } },
        onOpenRates = onOpenRates,
        onOpenPrecision = onOpenPrecision,
        onDone = onDone,
    )
}

@Composable
private fun LocalizationScreen(
    view: PortfolioView?,
    phoneIs24Hour: Boolean,
    onSave: (PortfolioCommand) -> Unit,
    onOpenRates: () -> Unit,
    onOpenPrecision: () -> Unit,
    onDone: () -> Unit,
) {
    BackScaffold(stringResource(R.string.localization_formatting), onDone) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(MaterialTheme.spacing.large))
            return@BackScaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionTitle(stringResource(R.string.language))
            AppDividedColumn(
                items =
                    listOf<@Composable () -> Unit>(
                        {
                            AppChoiceRow(
                                stringResource(R.string.language),
                                stringResource(languageLabel(view.language)),
                                AppLanguage.entries,
                                { stringResource(languageLabel(it)) },
                            ) { onSave(PortfolioCommand.SetLanguage(it)) }
                        },
                    ),
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            ) { _, row -> row() }
            SectionTitle(stringResource(R.string.currency))
            AppDividedColumn(
                items =
                    listOf<@Composable () -> Unit>(
                        { NavigationRow(stringResource(R.string.exchange_rates), onOpenRates) },
                        {
                            AppChoiceRow(
                                stringResource(R.string.primary_currency),
                                view.primaryCurrency,
                                view.currencies,
                                { it },
                            ) { onSave(PortfolioCommand.SetPrimaryCurrency(it)) }
                        },
                    ),
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            ) { _, row -> row() }
            SectionTitle(stringResource(R.string.calendar_format))
            AppDividedColumn(
                items =
                    listOf<@Composable () -> Unit>(
                        {
                            AppChoiceRow(
                                stringResource(R.string.date_picker_style),
                                stringResource(datePickerLabel(view.datePickerStyle)),
                                DatePickerStyle.entries,
                                { stringResource(datePickerLabel(it)) },
                            ) { onSave(PortfolioCommand.SetDatePickerStyle(it)) }
                        },
                        {
                            AppChoiceRow(
                                stringResource(R.string.time_picker_style),
                                stringResource(timePickerLabel(view.timePickerStyle)),
                                TimePickerStyle.entries,
                                { stringResource(timePickerLabel(it)) },
                            ) { onSave(PortfolioCommand.SetTimePickerStyle(it)) }
                        },
                        {
                            AppChoiceRow(
                                stringResource(R.string.clock_format),
                                clockLabel(view, phoneIs24Hour),
                                ClockFormat.entries,
                                { clockLabel(view, phoneIs24Hour, it) },
                            ) { onSave(PortfolioCommand.SetClockFormat(it)) }
                        },
                        {
                            AppChoiceRow(
                                stringResource(R.string.date_format),
                                stringResource(dateFormatLabel(view.dateFormat)),
                                DateFormat.entries,
                                { stringResource(dateFormatLabel(it)) },
                            ) { onSave(PortfolioCommand.SetDateFormat(it)) }
                        },
                        {
                            AppChoiceRow(
                                stringResource(R.string.date_order),
                                stringResource(dateOrderLabel(view.dateOrder)),
                                DateOrder.entries,
                                { stringResource(dateOrderLabel(it)) },
                            ) { onSave(PortfolioCommand.SetDateOrder(it)) }
                        },
                        {
                            AppChoiceRow(
                                stringResource(R.string.first_weekday),
                                stringResource(weekdayLabel(view.firstWeekday)),
                                FirstWeekday.entries,
                                { stringResource(weekdayLabel(it)) },
                            ) { onSave(PortfolioCommand.SetFirstWeekday(it)) }
                        },
                    ),
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            ) { _, row -> row() }
            SectionTitle(stringResource(R.string.number_format))
            val precisions = List(PRECISION_LIMIT + 1) { it }
            AppDividedColumn(
                items =
                    listOf<@Composable () -> Unit>(
                        { NavigationRow(stringResource(R.string.amount_precision), onOpenPrecision) },
                        {
                            AppChoiceRow(
                                stringResource(R.string.percentage_precision),
                                view.percentagePrecision.toString(),
                                precisions,
                                { it.toString() },
                            ) { onSave(PortfolioCommand.SetPercentagePrecision(it)) }
                        },
                    ),
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            ) { _, row -> row() }
        }
    }
}

@Composable
fun ExchangeRatesScreen(
    viewModel: PortfolioViewModel,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    ExchangeRatesScreen(
        view = portfolio,
        onDone = onDone,
        onSaveRate = { currency, text -> scope.launch { viewModel.commit(PortfolioCommand.SetRate(currency, text)) } },
    )
}

@Composable
private fun ExchangeRatesScreen(
    view: PortfolioView?,
    onDone: () -> Unit,
    onSaveRate: (String, String) -> Unit,
) {
    BackScaffold(stringResource(R.string.exchange_rates), onDone) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(MaterialTheme.spacing.large))
            return@BackScaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
        ) {
            view.rates.forEach { (currency, rate) ->
                RateRow(currency, rate) { text -> onSaveRate(currency, text) }
            }
            AddRateRow { currency, text -> onSaveRate(currency, text) }
        }
    }
}

@Composable
fun AmountPrecisionScreen(
    viewModel: PortfolioViewModel,
    onDone: () -> Unit,
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    AmountPrecisionScreen(
        view = portfolio,
        onDone = onDone,
        onSave = { currency, digits ->
            scope.launch { viewModel.commit(PortfolioCommand.SetAmountPrecision(currency, digits)) }
        },
    )
}

@Composable
private fun AmountPrecisionScreen(
    view: PortfolioView?,
    onDone: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    BackScaffold(stringResource(R.string.amount_precision), onDone) { padding ->
        if (view == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(MaterialTheme.spacing.large))
            return@BackScaffold
        }
        val precisions = List(PRECISION_LIMIT + 1) { it }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
        ) {
            view.currencies.forEach { currency ->
                val digits = view.amountPrecisions[currency] ?: 0
                AppChoiceRow(currency, digits.toString(), precisions, { it.toString() }) { selected ->
                    onSave(currency, selected)
                }
            }
        }
    }
}

@Composable
private fun RateRow(
    currency: String,
    rate: Exact,
    onSave: (String) -> Unit,
) {
    var text by remember(currency, rate) { mutableStateOf(rate.formatPlain()) }
    AppTextRow(currency, text, KeyboardType.Decimal) { text = it }
    Button(onClick = { onSave(text) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
}

@Composable
private fun AddRateRow(onSave: (String, String) -> Unit) {
    var currency by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    AppTextRow(stringResource(R.string.currency_code), currency) { currency = it }
    AppTextRow(stringResource(R.string.rate), rate, KeyboardType.Decimal) { rate = it }
    Button(onClick = { onSave(currency, rate) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.add_rate))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.small).padding(horizontal = MaterialTheme.spacing.medium))
}

@Composable
private fun NavigationRow(
    title: String,
    onOpen: () -> Unit,
) {
    Text(title, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(vertical = MaterialTheme.spacing.mediumSmall, horizontal = MaterialTheme.spacing.medium))
}

@Composable
private fun clockLabel(
    view: PortfolioView,
    phoneIs24Hour: Boolean,
    format: ClockFormat = view.clockFormat,
): String {
    val name =
        when (format) {
            ClockFormat.system -> stringResource(R.string.clock_system)
            ClockFormat.hour24 -> stringResource(R.string.clock_24)
            ClockFormat.hour12 -> stringResource(R.string.clock_12)
        }
    if (format != ClockFormat.system || view.clockFormat != ClockFormat.system) {
        return name
    }
    val resolved = if (phoneIs24Hour) stringResource(R.string.clock_24) else stringResource(R.string.clock_12)
    return "$name ($resolved)"
}

@Composable
private fun datePreview(
    view: PortfolioView,
    phone: DateOrder,
): String {
    val calendar = Calendar.getInstance()
    val today = CalendarDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    val order = resolveDateOrder(view.dateOrder, view.language, phone)
    return formatCalendarDay(today, today, view.dateFormat, order, view.language)
}

private fun languageLabel(language: AppLanguage): Int =
    when (language) {
        AppLanguage.english -> R.string.language_english
        AppLanguage.vietnamese -> R.string.language_vietnamese
    }

private fun datePickerLabel(style: DatePickerStyle): Int =
    when (style) {
        DatePickerStyle.calendar -> R.string.picker_calendar
        DatePickerStyle.wheels -> R.string.picker_wheels
    }

private fun timePickerLabel(style: TimePickerStyle): Int =
    when (style) {
        TimePickerStyle.clock -> R.string.picker_clock
        TimePickerStyle.wheels -> R.string.picker_wheels
    }

private fun dateFormatLabel(format: DateFormat): Int =
    when (format) {
        DateFormat.full -> R.string.date_full
        DateFormat.dateOnly -> R.string.date_only
        DateFormat.numeric -> R.string.date_numeric
        DateFormat.numericOnly -> R.string.date_numeric_only
    }

private fun dateOrderLabel(order: DateOrder): Int =
    when (order) {
        DateOrder.defaultOrder -> R.string.order_default
        DateOrder.system -> R.string.order_system
        DateOrder.monthDayYear -> R.string.order_mdy
        DateOrder.dayMonthYear -> R.string.order_dmy
        DateOrder.yearMonthDay -> R.string.order_ymd
        DateOrder.yearDayMonth -> R.string.order_ydm
    }

private fun weekdayLabel(weekday: FirstWeekday): Int =
    when (weekday) {
        FirstWeekday.monday -> R.string.weekday_monday
        FirstWeekday.sunday -> R.string.weekday_sunday
        FirstWeekday.saturday -> R.string.weekday_saturday
    }

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LocalizationScreenPreview() {
    ZenithTheme {
        LocalizationScreen(
            view = SamplePortfolio.view,
            phoneIs24Hour = true,
            onSave = {},
            onOpenRates = {},
            onOpenPrecision = {},
            onDone = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ExchangeRatesScreenPreview() {
    ZenithTheme {
        ExchangeRatesScreen(view = SamplePortfolio.view, onDone = {}, onSaveRate = { _, _ -> })
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AmountPrecisionScreenPreview() {
    ZenithTheme {
        AmountPrecisionScreen(view = SamplePortfolio.view, onDone = {}, onSave = { _, _ -> })
    }
}
