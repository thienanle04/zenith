import 'dart:convert';

import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/domain/money/exact.dart';

/// The book as a JSON document stored by Drift.
abstract final class BookCodec {
  static String encode(BookSnapshot book) => jsonEncode(_snapshot(book));

  static BookSnapshot decode(String raw) {
    if (raw.isEmpty) {
      return BookSnapshot.empty;
    }
    return _read(jsonDecode(raw) as Map<String, Object?>);
  }

  static Map<String, Object?> _snapshot(BookSnapshot book) => {
    'accounts': [
      for (final account in book.accounts)
        {
          'id': account.id,
          'name': account.name,
          'kind': account.kind.name,
          'currency': account.currency,
          'createdOrder': account.createdOrder,
          'setAside': account.setAside,
        },
    ],
    'movements': [
      for (final movement in book.movements)
        {'id': movement.id, 'accountId': movement.accountId, 'amountMinor': movement.amountMinor, 'vndPerUnit': movement.vndPerUnit.toJson()},
    ],
    'instruments': [
      for (final instrument in book.instruments)
        {
          'id': instrument.id,
          'name': instrument.name,
          'unit': instrument.unit,
          'priceCurrency': instrument.priceCurrency,
          'price': instrument.price.toJson(),
        },
    ],
    'holdings': [
      for (final holding in book.holdings)
        {'id': holding.id, 'instrumentId': holding.instrumentId, 'stockAccountId': holding.stockAccountId, 'createdOrder': holding.createdOrder},
    ],
    'adjustments': [
      for (final adjustment in book.adjustments)
        {'id': adjustment.id, 'holdingId': adjustment.holdingId, 'quantity': adjustment.quantity.toJson()},
    ],
    'rates': {for (final entry in book.rates.entries) entry.key: entry.value.toJson()},
    'sectionOrder': book.sectionOrder,
    'rowOrder': book.rowOrder,
    'separators': book.separators.name,
    'nextId': book.nextId,
  };

  static Map<String, Object?> _map(Object? value) => Map<String, Object?>.from(value! as Map);

  static BookSnapshot _read(Map<String, Object?> json) {
    return BookSnapshot(
      accounts: [
        for (final raw in json['accounts']! as List)
          AccountRecord(
            id: _map(raw)['id']! as String,
            name: _map(raw)['name']! as String,
            kind: AccountKind.values.byName(_map(raw)['kind']! as String),
            currency: _map(raw)['currency']! as String,
            createdOrder: _map(raw)['createdOrder']! as int,
            setAside: _map(raw)['setAside']! as bool,
          ),
      ],
      movements: [
        for (final raw in json['movements']! as List)
          MovementRecord(
            id: _map(raw)['id']! as String,
            accountId: _map(raw)['accountId']! as String,
            amountMinor: _map(raw)['amountMinor']! as int,
            vndPerUnit: Exact.fromJson(_map(raw)['vndPerUnit']! as String),
          ),
      ],
      instruments: [
        for (final raw in json['instruments']! as List)
          InstrumentRecord(
            id: _map(raw)['id']! as String,
            name: _map(raw)['name']! as String,
            unit: _map(raw)['unit']! as String,
            priceCurrency: _map(raw)['priceCurrency']! as String,
            price: Exact.fromJson(_map(raw)['price']! as String),
          ),
      ],
      holdings: [
        for (final raw in json['holdings']! as List)
          HoldingRecord(
            id: _map(raw)['id']! as String,
            instrumentId: _map(raw)['instrumentId']! as String,
            stockAccountId: _map(raw)['stockAccountId'] as String?,
            createdOrder: _map(raw)['createdOrder']! as int,
          ),
      ],
      adjustments: [
        for (final raw in json['adjustments']! as List)
          AdjustmentRecord(
            id: _map(raw)['id']! as String,
            holdingId: _map(raw)['holdingId']! as String,
            quantity: Exact.fromJson(_map(raw)['quantity']! as String),
          ),
      ],
      rates: {for (final entry in (json['rates']! as Map).entries) entry.key as String: Exact.fromJson(entry.value as String)},
      sectionOrder: [for (final key in json['sectionOrder']! as List) key as String],
      rowOrder: {
        for (final entry in (json['rowOrder']! as Map).entries) entry.key as String: [for (final id in entry.value as List) id as String],
      },
      separators: SeparatorPair.values.byName(json['separators']! as String),
      nextId: json['nextId']! as int,
    );
  }
}
