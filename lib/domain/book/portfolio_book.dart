import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/domain/money/currency.dart';
import 'package:zenith/domain/money/exact.dart';

enum SectionKind { assets, liabilities, shares, physical }

enum RowKind { account, holding }

final class PortfolioView {
  const PortfolioView({
    required this.assets,
    required this.liabilities,
    required this.netWorth,
    required this.sections,
    required this.separators,
    required this.currencies,
    required this.stockAccounts,
  });

  /// Signed VND đồng, already rounded.
  final int assets;
  final int liabilities;
  final int netWorth;
  final List<PortfolioSection> sections;
  final SeparatorPair separators;
  final List<String> currencies;
  final List<StockAccountChoice> stockAccounts;
}

final class StockAccountChoice {
  const StockAccountChoice({required this.id, required this.name});

  final String id;
  final String name;
}

final class PortfolioSection {
  const PortfolioSection({required this.key, required this.kind, required this.rows, this.heading});

  final String key;
  final SectionKind kind;
  final String? heading;
  final List<PortfolioRow> rows;
}

final class PortfolioRow {
  const PortfolioRow.account({required this.id, required this.title, required this.minor, required this.currency})
    : kind = RowKind.account,
      quantity = null,
      unit = null,
      value = null,
      valueCurrency = null;

  const PortfolioRow.holding({
    required this.id,
    required this.title,
    required this.quantity,
    required this.unit,
    required this.value,
    required this.valueCurrency,
  }) : kind = RowKind.holding,
       minor = null,
       currency = null;

  final String id;
  final RowKind kind;
  final String title;
  final int? minor;
  final String? currency;
  final Exact? quantity;
  final String? unit;
  final Exact? value;
  final String? valueCurrency;
}

final class AccountEditor {
  const AccountEditor({
    required this.id,
    required this.name,
    required this.kind,
    required this.currency,
    required this.balance,
    required this.currencies,
    required this.rate,
    required this.separators,
  });

  final String id;
  final String name;
  final AccountKind kind;
  final String currency;
  final Exact balance;
  final List<String> currencies;
  final Exact rate;
  final SeparatorPair separators;
}

final class HoldingEditor {
  const HoldingEditor({
    required this.holdingId,
    required this.instrumentId,
    required this.name,
    required this.unit,
    required this.priceCurrency,
    required this.price,
    required this.quantity,
    required this.separators,
  });

  final String holdingId;
  final String instrumentId;
  final String name;
  final String unit;
  final String priceCurrency;
  final Exact price;
  final Exact quantity;
  final SeparatorPair separators;
}

/// The portfolio commands and the read model the page shows.
abstract final class PortfolioBook {
  static PortfolioView present(BookSnapshot book) {
    final sections = _sections(book);
    final assets = _sumSide(book, positive: true);
    final liabilities = _sumSide(book, positive: false);
    return PortfolioView(
      assets: assets,
      liabilities: liabilities,
      netWorth: assets + liabilities,
      sections: sections,
      separators: book.separators,
      currencies: _currencies(book),
      stockAccounts: [
        for (final account in book.accounts.where((account) => account.kind == AccountKind.stock))
          StockAccountChoice(id: account.id, name: account.name),
      ],
    );
  }

  static AccountEditor accountEditor(BookSnapshot book, String id) {
    final account = _account(book, id);
    return AccountEditor(
      id: account.id,
      name: account.name,
      kind: account.kind,
      currency: account.currency,
      balance: majorAmount(_balance(book, id), account.currency),
      currencies: _currencies(book),
      rate: _rate(book, account.currency),
      separators: book.separators,
    );
  }

  static HoldingEditor holdingEditor(BookSnapshot book, String id) {
    final holding = _holding(book, id);
    final instrument = _instrument(book, holding.instrumentId);
    return HoldingEditor(
      holdingId: holding.id,
      instrumentId: instrument.id,
      name: instrument.name,
      unit: instrument.unit,
      priceCurrency: instrument.priceCurrency,
      price: instrument.price,
      quantity: _quantity(book, holding.id),
      separators: book.separators,
    );
  }

  /// The balance a currency change would produce, in the new currency's major units.
  static Exact proposedBalance(BookSnapshot book, String id, String newCurrency, Exact newRate) {
    final account = _account(book, id);
    if (newRate.isZero || newRate.isNegative) {
      throw const BookFailure('ratePositive');
    }
    final minor = _balance(book, id);
    if (minor == 0) {
      return Exact.zero;
    }
    final vnd = majorAmount(minor, account.currency) * _rate(book, account.currency);
    return vnd / newRate;
  }

  static BookSnapshot addAccount(BookSnapshot book, {required String name, required AccountKind kind, required String currency}) {
    _requireName(name);
    _requireCurrency(book, currency);
    final id = _id(book);
    return book.copy(
      accounts: [
        ...book.accounts,
        AccountRecord(id: id, name: name.trim(), kind: kind, currency: currency, createdOrder: book.nextId),
      ],
      nextId: book.nextId + 1,
    );
  }

  static BookSnapshot saveAccount(
    BookSnapshot book, {
    required String id,
    required String name,
    required AccountKind kind,
    required String currency,
    required Exact balance,
    Exact? rate,
  }) {
    _requireName(name);
    _requireCurrency(book, currency);
    final account = _account(book, id);
    if (account.kind == AccountKind.stock && kind != AccountKind.stock) {
      throw const BookFailure('stockKindFixed');
    }
    if (account.kind != AccountKind.stock && kind == AccountKind.stock) {
      throw const BookFailure('stockKindFixed');
    }
    var next = book;
    if (currency != account.currency) {
      final appliedRate = rate ?? _rate(book, currency);
      if (appliedRate.isZero || appliedRate.isNegative) {
        throw const BookFailure('ratePositive');
      }
      next = _rescale(next, account, currency, balance);
    } else {
      next = _setBalance(next, id, minorAmount(balance, currency));
    }
    return next.copy(
      accounts: [
        for (final item in next.accounts)
          if (item.id == id) item.copy(name: name.trim(), kind: kind, currency: currency) else item,
      ],
    );
  }

  static BookSnapshot setAside(BookSnapshot book, String id) {
    _account(book, id);
    return book.copy(
      accounts: [for (final account in book.accounts) if (account.id == id) account.copy(setAside: true) else account],
    );
  }

  static BookSnapshot addInstrument(
    BookSnapshot book, {
    required String name,
    required String unit,
    required String priceCurrency,
    required Exact price,
    required Exact quantity,
    String? stockAccountId,
  }) {
    _requireName(name);
    _requireName(unit);
    _requireCurrency(book, priceCurrency);
    if (stockAccountId != null) {
      final account = _account(book, stockAccountId);
      if (account.kind != AccountKind.stock) {
        throw const BookFailure('stockAccountRequired');
      }
    }
    final instrumentId = _id(book);
    final holdingId = '${book.nextId + 1}';
    var next = book.copy(
      instruments: [
        ...book.instruments,
        InstrumentRecord(id: instrumentId, name: name.trim(), unit: unit.trim(), priceCurrency: priceCurrency, price: price),
      ],
      holdings: [
        ...book.holdings,
        HoldingRecord(id: holdingId, instrumentId: instrumentId, stockAccountId: stockAccountId, createdOrder: book.nextId + 1),
      ],
      nextId: book.nextId + 2,
    );
    if (!quantity.isZero) {
      next = next.copy(
        adjustments: [
          ...next.adjustments,
          AdjustmentRecord(id: _id(next), holdingId: holdingId, quantity: quantity),
        ],
        nextId: next.nextId + 1,
      );
    }
    return next;
  }

  static BookSnapshot saveHolding(
    BookSnapshot book, {
    required String holdingId,
    required String name,
    required Exact price,
    required Exact quantity,
  }) {
    _requireName(name);
    final holding = _holding(book, holdingId);
    final instrument = _instrument(book, holding.instrumentId);
    var next = _setQuantity(book, holdingId, quantity);
    next = next.copy(
      instruments: [
        for (final item in next.instruments)
          if (item.id == instrument.id) item.copy(name: name.trim(), price: price) else item,
      ],
    );
    return next;
  }

  static BookSnapshot removeHolding(BookSnapshot book, String holdingId) {
    _holding(book, holdingId);
    return book.copy(
      holdings: [for (final holding in book.holdings) if (holding.id != holdingId) holding],
      adjustments: [for (final adjustment in book.adjustments) if (adjustment.holdingId != holdingId) adjustment],
    );
  }

  static BookSnapshot removeInstrument(BookSnapshot book, String instrumentId) {
    _instrument(book, instrumentId);
    final holdingIds = {for (final holding in book.holdings.where((holding) => holding.instrumentId == instrumentId)) holding.id};
    return book.copy(
      instruments: [for (final instrument in book.instruments) if (instrument.id != instrumentId) instrument],
      holdings: [for (final holding in book.holdings) if (!holdingIds.contains(holding.id)) holding],
      adjustments: [for (final adjustment in book.adjustments) if (!holdingIds.contains(adjustment.holdingId)) adjustment],
    );
  }

  static BookSnapshot moveSection(BookSnapshot book, String key, int delta) {
    final order = _mergedSectionOrder(book, _presentSectionKeys(book));
    final index = order.indexOf(key);
    final target = index + delta;
    if (index < 0 || target < 0 || target >= order.length) {
      return book;
    }
    final next = [...order];
    final moved = next.removeAt(index);
    next.insert(target, moved);
    return book.copy(sectionOrder: next);
  }

  static BookSnapshot moveRow(BookSnapshot book, String sectionKey, String rowId, int delta) {
    final order = _rowIds(book, sectionKey);
    final index = order.indexOf(rowId);
    final target = index + delta;
    if (index < 0 || target < 0 || target >= order.length) {
      return book;
    }
    final next = [...order];
    final moved = next.removeAt(index);
    next.insert(target, moved);
    return book.copy(rowOrder: {...book.rowOrder, sectionKey: next});
  }

  static int _sumSide(BookSnapshot book, {required bool positive}) {
    var sum = Exact.zero;
    for (final account in book.accounts) {
      final contribution = _accountVnd(book, account);
      if (positive && contribution.isPositive) {
        sum += contribution;
      }
      if (!positive && contribution.isNegative) {
        sum += contribution;
      }
    }
    for (final holding in book.holdings) {
      final contribution = _holdingVnd(book, holding);
      if (positive && contribution.isPositive) {
        sum += contribution;
      }
      if (!positive && contribution.isNegative) {
        sum += contribution;
      }
    }
    return sum.roundToIntChecked();
  }

  static List<PortfolioSection> _sections(BookSnapshot book) {
    final keys = _mergedSectionOrder(book, _presentSectionKeys(book));
    return [for (final key in keys) _section(book, key)];
  }

  static Set<String> _presentSectionKeys(BookSnapshot book) {
    final keys = <String>{};
    for (final account in book.accounts.where((account) => !account.setAside)) {
      keys.add(_balance(book, account.id) < 0 ? 'liabilities' : 'assets');
    }
    for (final holding in book.holdings) {
      final stock = holding.stockAccountId;
      keys.add(stock == null ? 'physical' : 'shares:$stock');
    }
    return keys;
  }

  static List<String> _defaultSectionOrder(BookSnapshot book, Set<String> present) {
    final keys = <String>[];
    if (present.contains('assets')) {
      keys.add('assets');
    }
    final stocks = book.accounts.where((account) => account.kind == AccountKind.stock).toList()
      ..sort((a, b) => a.createdOrder.compareTo(b.createdOrder));
    for (final account in stocks) {
      final key = 'shares:${account.id}';
      if (present.contains(key)) {
        keys.add(key);
      }
    }
    if (present.contains('physical')) {
      keys.add('physical');
    }
    if (present.contains('liabilities')) {
      keys.add('liabilities');
    }
    return keys;
  }

  static List<String> _mergedSectionOrder(BookSnapshot book, Set<String> present) {
    final defaults = _defaultSectionOrder(book, present);
    final result = [for (final key in book.sectionOrder) if (present.contains(key)) key];
    for (var i = 0; i < defaults.length; i++) {
      final key = defaults[i];
      if (result.contains(key)) {
        continue;
      }
      final later = defaults.skip(i + 1).toSet();
      final index = result.indexWhere(later.contains);
      if (index == -1) {
        result.add(key);
      } else {
        result.insert(index, key);
      }
    }
    return result;
  }

  static PortfolioSection _section(BookSnapshot book, String key) {
    if (key == 'assets' || key == 'liabilities') {
      final negative = key == 'liabilities';
      final accounts = book.accounts.where((account) {
        if (account.setAside) {
          return false;
        }
        final balance = _balance(book, account.id);
        return negative ? balance < 0 : balance >= 0;
      }).toList();
      final ordered = _order(accounts, book.rowOrder[key] ?? const [], (account) => account.id, (account) => account.createdOrder);
      return PortfolioSection(
        key: key,
        kind: negative ? SectionKind.liabilities : SectionKind.assets,
        rows: [
          for (final account in ordered)
            PortfolioRow.account(id: account.id, title: account.name, minor: _balance(book, account.id), currency: account.currency),
        ],
      );
    }
    if (key == 'physical') {
      final holdings = book.holdings.where((holding) => holding.stockAccountId == null).toList();
      return PortfolioSection(key: key, kind: SectionKind.physical, rows: _holdingRows(book, key, holdings));
    }
    final accountId = key.substring('shares:'.length);
    final account = _account(book, accountId);
    final holdings = book.holdings.where((holding) => holding.stockAccountId == accountId).toList();
    return PortfolioSection(key: key, kind: SectionKind.shares, heading: account.name, rows: _holdingRows(book, key, holdings));
  }

  static List<PortfolioRow> _holdingRows(BookSnapshot book, String key, List<HoldingRecord> holdings) {
    final ordered = _order(holdings, book.rowOrder[key] ?? const [], (holding) => holding.id, (holding) => holding.createdOrder);
    return [
      for (final holding in ordered)
        PortfolioRow.holding(
          id: holding.id,
          title: _instrument(book, holding.instrumentId).name,
          quantity: _quantity(book, holding.id),
          unit: _instrument(book, holding.instrumentId).unit,
          value: _quantity(book, holding.id) * _instrument(book, holding.instrumentId).price,
          valueCurrency: _instrument(book, holding.instrumentId).priceCurrency,
        ),
    ];
  }

  static List<T> _order<T>(List<T> items, List<String> saved, String Function(T) id, int Function(T) created) {
    final rank = {for (var i = 0; i < saved.length; i++) saved[i]: i};
    final copy = [...items];
    copy.sort((a, b) {
      final left = rank[id(a)];
      final right = rank[id(b)];
      if (left == null && right == null) {
        return created(a).compareTo(created(b));
      }
      if (left == null) {
        return 1;
      }
      if (right == null) {
        return -1;
      }
      return left.compareTo(right);
    });
    return copy;
  }

  static List<String> _rowIds(BookSnapshot book, String key) => [for (final row in _section(book, key).rows) row.id];

  static BookSnapshot _setBalance(BookSnapshot book, String id, int targetMinor) {
    final current = _balance(book, id);
    if (current == targetMinor) {
      return _placeAccount(book, id, targetMinor);
    }
    final account = _account(book, id);
    final next = book.copy(
      movements: [
        ...book.movements,
        MovementRecord(id: _id(book), accountId: id, amountMinor: targetMinor - current, vndPerUnit: _rate(book, account.currency)),
      ],
      nextId: book.nextId + 1,
    );
    return _placeAccount(next, id, targetMinor);
  }

  static BookSnapshot _placeAccount(BookSnapshot book, String id, int minor) {
    final destination = minor < 0 ? 'liabilities' : 'assets';
    final rowOrder = {...book.rowOrder};
    for (final key in ['assets', 'liabilities']) {
      final list = [...rowOrder[key] ?? const <String>[]];
      if (list.remove(id) && key != destination) {
        rowOrder[key] = list;
      } else if (key != destination) {
        rowOrder[key] = list;
      }
    }
    final destinationRows = [...rowOrder[destination] ?? const <String>[]];
    if (!destinationRows.contains(id)) {
      destinationRows.add(id);
    }
    rowOrder[destination] = destinationRows;
    return book.copy(rowOrder: rowOrder);
  }

  static BookSnapshot _rescale(BookSnapshot book, AccountRecord account, String currency, Exact balance) {
    final current = _balance(book, account.id);
    final target = minorAmount(balance, currency);
    if (current == 0) {
      return book;
    }
    final movements = book.movements.where((movement) => movement.accountId == account.id).toList();
    var allocated = 0;
    final rewritten = <MovementRecord>[];
    for (var i = 0; i < movements.length; i++) {
      final movement = movements[i];
      final int amount;
      if (i == movements.length - 1) {
        amount = target - allocated;
      } else {
        amount = (Exact.fromInt(movement.amountMinor) * Exact.fromInt(target) / Exact.fromInt(current)).roundToIntChecked();
        allocated += amount;
      }
      rewritten.add(MovementRecord(id: movement.id, accountId: movement.accountId, amountMinor: amount, vndPerUnit: _rate(book, currency)));
    }
    return _placeAccount(
      book.copy(
        movements: [
          for (final movement in book.movements)
            if (movement.accountId == account.id) rewritten.firstWhere((item) => item.id == movement.id) else movement,
        ],
      ),
      account.id,
      target,
    );
  }

  static BookSnapshot _setQuantity(BookSnapshot book, String holdingId, Exact target) {
    final current = _quantity(book, holdingId);
    if (current == target) {
      return book;
    }
    return book.copy(
      adjustments: [
        ...book.adjustments,
        AdjustmentRecord(id: _id(book), holdingId: holdingId, quantity: target - current),
      ],
      nextId: book.nextId + 1,
    );
  }

  static int _balance(BookSnapshot book, String id) =>
      book.movements.where((movement) => movement.accountId == id).fold(0, (sum, movement) => sum + movement.amountMinor);

  static Exact _quantity(BookSnapshot book, String id) => book.adjustments
      .where((adjustment) => adjustment.holdingId == id)
      .fold(Exact.zero, (sum, adjustment) => sum + adjustment.quantity);

  static Exact _accountVnd(BookSnapshot book, AccountRecord account) {
    if (account.setAside) {
      return Exact.zero;
    }
    final minor = _balance(book, account.id);
    if (minor == 0) {
      return Exact.zero;
    }
    return majorAmount(minor, account.currency) * _rate(book, account.currency);
  }

  static Exact _holdingVnd(BookSnapshot book, HoldingRecord holding) {
    final instrument = _instrument(book, holding.instrumentId);
    final value = _quantity(book, holding.id) * instrument.price;
    if (value.isZero) {
      return Exact.zero;
    }
    return value * _rate(book, instrument.priceCurrency);
  }

  static Exact _rate(BookSnapshot book, String currency) {
    if (currency == 'VND') {
      return Exact.one;
    }
    final rate = book.rates[currency];
    if (rate == null) {
      throw const BookFailure('currencyNeedsRate');
    }
    return rate;
  }

  static List<String> _currencies(BookSnapshot book) => ['VND', ...book.rates.keys];

  static void _requireCurrency(BookSnapshot book, String currency) {
    if (currency != 'VND' && !book.rates.containsKey(currency)) {
      throw const BookFailure('currencyNeedsRate');
    }
  }

  static void _requireName(String name) {
    if (name.trim().isEmpty) {
      throw const BookFailure('nameRequired');
    }
  }

  static AccountRecord _account(BookSnapshot book, String id) =>
      book.accounts.firstWhere((account) => account.id == id, orElse: () => throw const BookFailure('notFound'));

  static HoldingRecord _holding(BookSnapshot book, String id) =>
      book.holdings.firstWhere((holding) => holding.id == id, orElse: () => throw const BookFailure('notFound'));

  static InstrumentRecord _instrument(BookSnapshot book, String id) => book.instruments.firstWhere(
    (instrument) => instrument.id == id,
    orElse: () => throw const BookFailure('notFound'),
  );

  static String _id(BookSnapshot book) => '${book.nextId}';
}
