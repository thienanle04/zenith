import 'package:flutter_test/flutter_test.dart';
import 'package:zenith/domain/book/portfolio_book.dart';
import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/domain/money/exact.dart';

void main() {
  test('net worth is assets plus liabilities, and a negative bank is a liability', () {
    var book = BookSnapshot.empty;
    book = PortfolioBook.addAccount(book, name: 'Cash', kind: AccountKind.cash, currency: 'VND');
    book = PortfolioBook.addAccount(book, name: 'Bank', kind: AccountKind.bank, currency: 'VND');
    book = PortfolioBook.addAccount(book, name: 'Card', kind: AccountKind.creditCard, currency: 'VND');
    book = PortfolioBook.addAccount(book, name: 'Loan', kind: AccountKind.loan, currency: 'VND');
    book = PortfolioBook.saveAccount(book, id: '1', name: 'Cash', kind: AccountKind.cash, currency: 'VND', balance: Exact.parse('10000000'));
    book = PortfolioBook.saveAccount(book, id: '2', name: 'Bank', kind: AccountKind.bank, currency: 'VND', balance: Exact.parse('-100000'));
    book = PortfolioBook.saveAccount(book, id: '3', name: 'Card', kind: AccountKind.creditCard, currency: 'VND', balance: Exact.parse('-2000000'));
    book = PortfolioBook.saveAccount(book, id: '4', name: 'Loan', kind: AccountKind.loan, currency: 'VND', balance: Exact.parse('-1000000'));
    book = PortfolioBook.addAccount(book, name: 'SSI', kind: AccountKind.stock, currency: 'VND');
    book = PortfolioBook.addInstrument(
      book,
      name: 'FPT',
      unit: 'share',
      priceCurrency: 'VND',
      price: Exact.parse('50000'),
      quantity: Exact.parse('100'),
      stockAccountId: '9',
    );
    book = PortfolioBook.addInstrument(
      book,
      name: 'Gold',
      unit: 'lượng',
      priceCurrency: 'VND',
      price: Exact.parse('3000000'),
      quantity: Exact.one,
    );

    final view = PortfolioBook.present(book);

    expect(view.assets, 18000000);
    expect(view.liabilities, -3100000);
    expect(view.netWorth, 14900000);
    expect(view.sections.map((section) => section.kind), [SectionKind.assets, SectionKind.shares, SectionKind.physical, SectionKind.liabilities]);
    expect(view.sections[1].heading, 'SSI');
    expect(view.sections[1].rows.single.title, 'FPT');
  });

  test('setting an account aside drops its balance and keeps a share holding', () {
    var book = BookSnapshot.empty;
    book = PortfolioBook.addAccount(book, name: 'SSI', kind: AccountKind.stock, currency: 'VND');
    book = PortfolioBook.saveAccount(book, id: '1', name: 'SSI', kind: AccountKind.stock, currency: 'VND', balance: Exact.parse('5000000'));
    book = PortfolioBook.addInstrument(
      book,
      name: 'FPT',
      unit: 'share',
      priceCurrency: 'VND',
      price: Exact.parse('50000'),
      quantity: Exact.parse('100'),
      stockAccountId: '1',
    );

    book = PortfolioBook.setAside(book, '1');
    final view = PortfolioBook.present(book);

    expect(view.assets, 5000000);
    expect(view.liabilities, 0);
    expect(view.netWorth, 5000000);
    expect(view.sections.map((section) => section.kind), [SectionKind.shares]);
    expect(book.movements, isNotEmpty);
  });

  test('typing the same balance records nothing, and a new account starts at zero', () {
    var book = PortfolioBook.addAccount(BookSnapshot.empty, name: 'Cash', kind: AccountKind.cash, currency: 'VND');
    expect(PortfolioBook.present(book).assets, 0);
    expect(book.movements, isEmpty);

    book = PortfolioBook.saveAccount(book, id: '1', name: 'Cash', kind: AccountKind.cash, currency: 'VND', balance: Exact.parse('46'));
    book = PortfolioBook.saveAccount(book, id: '1', name: 'Cash', kind: AccountKind.cash, currency: 'VND', balance: Exact.parse('46'));

    expect(book.movements, hasLength(1));
  });

  test('removing a holding removes its quantity', () {
    var book = PortfolioBook.addAccount(BookSnapshot.empty, name: 'SSI', kind: AccountKind.stock, currency: 'VND');
    book = PortfolioBook.addInstrument(
      book,
      name: 'FPT',
      unit: 'share',
      priceCurrency: 'VND',
      price: Exact.parse('50000'),
      quantity: Exact.parse('100'),
      stockAccountId: '1',
    );

    book = PortfolioBook.removeHolding(book, '3');

    expect(book.holdings, isEmpty);
    expect(book.adjustments, isEmpty);
    expect(PortfolioBook.present(book).assets, 0);
  });

  test('a balance that crosses zero lands at the end of the other section', () {
    var book = PortfolioBook.addAccount(BookSnapshot.empty, name: 'Cash', kind: AccountKind.cash, currency: 'VND');
    book = PortfolioBook.addAccount(book, name: 'Bank', kind: AccountKind.bank, currency: 'VND');
    book = PortfolioBook.saveAccount(book, id: '1', name: 'Cash', kind: AccountKind.cash, currency: 'VND', balance: Exact.parse('10'));
    book = PortfolioBook.saveAccount(book, id: '2', name: 'Bank', kind: AccountKind.bank, currency: 'VND', balance: Exact.parse('20'));
    book = PortfolioBook.saveAccount(book, id: '1', name: 'Cash', kind: AccountKind.cash, currency: 'VND', balance: Exact.parse('-5'));

    final liabilities = PortfolioBook.present(book).sections.singleWhere((section) => section.kind == SectionKind.liabilities);
    expect(liabilities.rows.single.title, 'Cash');
  });

  test('a foreign balance converts through the rate list', () {
    final book = PortfolioBook.saveAccount(
      PortfolioBook.addAccount(
        BookSnapshot(rates: {'USD': Exact.parse('25000')}),
        name: 'Dollar',
        kind: AccountKind.bank,
        currency: 'USD',
      ),
      id: '1',
      name: 'Dollar',
      kind: AccountKind.bank,
      currency: 'USD',
      balance: Exact.parse('3'),
    );

    expect(PortfolioBook.present(book).assets, 75000);
  });
}
