import 'package:zenith/domain/money/exact.dart';

enum AccountKind { cash, bank, eWallet, creditCard, loan, receivable, stock }

enum SeparatorPair { commaPoint, pointComma }

/// One owner's book, as stored.
final class BookSnapshot {
  const BookSnapshot({
    this.accounts = const [],
    this.movements = const [],
    this.instruments = const [],
    this.holdings = const [],
    this.adjustments = const [],
    this.rates = const {},
    this.sectionOrder = const [],
    this.rowOrder = const {},
    this.separators = SeparatorPair.commaPoint,
    this.nextId = 1,
  });

  final List<AccountRecord> accounts;
  final List<MovementRecord> movements;
  final List<InstrumentRecord> instruments;
  final List<HoldingRecord> holdings;
  final List<AdjustmentRecord> adjustments;
  final Map<String, Exact> rates;
  final List<String> sectionOrder;
  final Map<String, List<String>> rowOrder;
  final SeparatorPair separators;
  final int nextId;

  static const empty = BookSnapshot();

  BookSnapshot copy({
    List<AccountRecord>? accounts,
    List<MovementRecord>? movements,
    List<InstrumentRecord>? instruments,
    List<HoldingRecord>? holdings,
    List<AdjustmentRecord>? adjustments,
    Map<String, Exact>? rates,
    List<String>? sectionOrder,
    Map<String, List<String>>? rowOrder,
    SeparatorPair? separators,
    int? nextId,
  }) {
    return BookSnapshot(
      accounts: accounts ?? this.accounts,
      movements: movements ?? this.movements,
      instruments: instruments ?? this.instruments,
      holdings: holdings ?? this.holdings,
      adjustments: adjustments ?? this.adjustments,
      rates: rates ?? this.rates,
      sectionOrder: sectionOrder ?? this.sectionOrder,
      rowOrder: rowOrder ?? this.rowOrder,
      separators: separators ?? this.separators,
      nextId: nextId ?? this.nextId,
    );
  }
}

final class AccountRecord {
  const AccountRecord({
    required this.id,
    required this.name,
    required this.kind,
    required this.currency,
    required this.createdOrder,
    this.setAside = false,
  });

  final String id;
  final String name;
  final AccountKind kind;
  final String currency;
  final int createdOrder;
  final bool setAside;

  AccountRecord copy({String? name, AccountKind? kind, String? currency, bool? setAside}) {
    return AccountRecord(
      id: id,
      name: name ?? this.name,
      kind: kind ?? this.kind,
      currency: currency ?? this.currency,
      createdOrder: createdOrder,
      setAside: setAside ?? this.setAside,
    );
  }
}

final class MovementRecord {
  const MovementRecord({required this.id, required this.accountId, required this.amountMinor, required this.vndPerUnit});

  final String id;
  final String accountId;
  final int amountMinor;
  final Exact vndPerUnit;
}

final class InstrumentRecord {
  const InstrumentRecord({
    required this.id,
    required this.name,
    required this.unit,
    required this.priceCurrency,
    required this.price,
  });

  final String id;
  final String name;
  final String unit;
  final String priceCurrency;
  final Exact price;

  InstrumentRecord copy({String? name, Exact? price}) {
    return InstrumentRecord(
      id: id,
      name: name ?? this.name,
      unit: unit,
      priceCurrency: priceCurrency,
      price: price ?? this.price,
    );
  }
}

final class HoldingRecord {
  const HoldingRecord({required this.id, required this.instrumentId, required this.createdOrder, this.stockAccountId});

  final String id;
  final String instrumentId;
  final String? stockAccountId;
  final int createdOrder;
}

final class AdjustmentRecord {
  const AdjustmentRecord({required this.id, required this.holdingId, required this.quantity});

  final String id;
  final String holdingId;
  final Exact quantity;
}

/// A rule the book refuses to break.
final class BookFailure implements Exception {
  const BookFailure(this.code);

  final String code;

  @override
  String toString() => code;
}
