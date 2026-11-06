import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zenith/app.dart';
import 'package:zenith/domain/book/book_repository.dart';
import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/domain/money/exact.dart';
import 'package:zenith/presentation/portfolio/money_format.dart';
import 'package:zenith/presentation/providers/providers.dart';

void main() {
  test('money uses the sign and the separator pair', () {
    expect(formatMinor(10000000, 'VND', SeparatorPair.commaPoint), '10,000,000đ');
    expect(formatMinor(-100000, 'VND', SeparatorPair.commaPoint), '100,000đ');
    expect(formatMoney(Exact.parse('3.5'), 'USD', SeparatorPair.commaPoint), r'$ 3.5');
    expect(formatMoney(Exact.parse('3.5'), 'EUR', SeparatorPair.pointComma), '€ 3,5');
    expect(formatQuantity(Exact.parse('10000.5'), 'lượng', SeparatorPair.commaPoint), '10,000.5 lượng');
  });

  testWidgets('an empty book shows three zero figures', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [bookRepositoryProvider.overrideWithValue(_MemoryBook())],
        child: const ZenithApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('0đ'), findsNWidgets(3));
    expect(find.text('Assets'), findsOneWidget);
    expect(find.text('Liabilities'), findsOneWidget);
    expect(find.text('Net worth'), findsOneWidget);
  });
}

class _MemoryBook implements BookRepository {
  BookSnapshot book = BookSnapshot.empty;

  @override
  Future<BookSnapshot> load() async => book;

  @override
  Future<void> save(BookSnapshot next) async => book = next;
}
