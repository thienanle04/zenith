// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'Zenith';

  @override
  String get portfolio => 'Portfolio';

  @override
  String get assets => 'Assets';

  @override
  String get liabilities => 'Liabilities';

  @override
  String get netWorth => 'Net worth';

  @override
  String get physicalHoldings => 'Physical';

  @override
  String get addAccount => 'Add account';

  @override
  String get addHolding => 'Add holding';

  @override
  String get name => 'Name';

  @override
  String get kind => 'Kind';

  @override
  String get currency => 'Currency';

  @override
  String get balance => 'Balance';

  @override
  String get unit => 'Unit';

  @override
  String get price => 'Price';

  @override
  String get quantity => 'Quantity';

  @override
  String get save => 'Save';

  @override
  String get setAside => 'Set aside';

  @override
  String get removeHolding => 'Remove holding';

  @override
  String get removeInstrument => 'Remove instrument';

  @override
  String get shares => 'Shares';

  @override
  String get physical => 'Physical';

  @override
  String get rate => 'Rate';

  @override
  String get moveUp => 'Move up';

  @override
  String get moveDown => 'Move down';

  @override
  String get cash => 'Cash';

  @override
  String get bank => 'Bank';

  @override
  String get eWallet => 'E-wallet';

  @override
  String get creditCard => 'Credit card';

  @override
  String get loan => 'Loan';

  @override
  String get receivable => 'Receivable';

  @override
  String get stock => 'Stock account';

  @override
  String get nameRequired => 'A name is required.';

  @override
  String get currencyNeedsRate => 'That currency needs a rate first.';

  @override
  String get stockKindFixed => 'A stock account stays a stock account.';

  @override
  String get stockAccountRequired => 'Shares belong to a stock account.';

  @override
  String get ratePositive => 'The rate has to be positive.';

  @override
  String get notFound => 'That record is no longer in the book.';

  @override
  String get invalidAmount => 'Enter a number.';
}
