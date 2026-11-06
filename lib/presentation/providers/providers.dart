import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:zenith/core/db/database.dart';
import 'package:zenith/core/db/drift_book_repository.dart';
import 'package:zenith/domain/book/book_repository.dart';
import 'package:zenith/domain/book/portfolio_book.dart';
import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/domain/money/exact.dart';

final databaseProvider = Provider<ZenithDatabase>((ref) {
  final database = ZenithDatabase();
  ref.onDispose(database.close);
  return database;
});

final bookRepositoryProvider = Provider<BookRepository>((ref) => DriftBookRepository(ref.watch(databaseProvider)));

final portfolioProvider = AsyncNotifierProvider<PortfolioNotifier, PortfolioView>(PortfolioNotifier.new);

/// Loads the book and applies the portfolio commands.
class PortfolioNotifier extends AsyncNotifier<PortfolioView> {
  @override
  Future<PortfolioView> build() async => PortfolioBook.present(await ref.watch(bookRepositoryProvider).load());

  Future<void> _commit(BookSnapshot Function(BookSnapshot book) change) async {
    final repository = ref.read(bookRepositoryProvider);
    final next = change(await repository.load());
    await repository.save(next);
    state = AsyncData(PortfolioBook.present(next));
  }

  Future<void> addAccount({required String name, required AccountKind kind, required String currency}) =>
      _commit((book) => PortfolioBook.addAccount(book, name: name, kind: kind, currency: currency));

  Future<void> saveAccount({
    required String id,
    required String name,
    required AccountKind kind,
    required String currency,
    required Exact balance,
    Exact? rate,
  }) => _commit(
    (book) => PortfolioBook.saveAccount(book, id: id, name: name, kind: kind, currency: currency, balance: balance, rate: rate),
  );

  Future<void> setAside(String id) => _commit((book) => PortfolioBook.setAside(book, id));

  Future<void> addInstrument({
    required String name,
    required String unit,
    required String priceCurrency,
    required Exact price,
    required Exact quantity,
    String? stockAccountId,
  }) => _commit(
    (book) => PortfolioBook.addInstrument(
      book,
      name: name,
      unit: unit,
      priceCurrency: priceCurrency,
      price: price,
      quantity: quantity,
      stockAccountId: stockAccountId,
    ),
  );

  Future<void> saveHolding({required String holdingId, required String name, required Exact price, required Exact quantity}) =>
      _commit((book) => PortfolioBook.saveHolding(book, holdingId: holdingId, name: name, price: price, quantity: quantity));

  Future<void> removeHolding(String holdingId) => _commit((book) => PortfolioBook.removeHolding(book, holdingId));

  Future<void> removeInstrument(String instrumentId) => _commit((book) => PortfolioBook.removeInstrument(book, instrumentId));

  Future<void> moveSection(String key, int delta) => _commit((book) => PortfolioBook.moveSection(book, key, delta));

  Future<void> moveRow(String sectionKey, String rowId, int delta) =>
      _commit((book) => PortfolioBook.moveRow(book, sectionKey, rowId, delta));
}
