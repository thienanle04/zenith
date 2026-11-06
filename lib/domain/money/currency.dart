import 'package:zenith/domain/money/exact.dart';

/// Minor-unit scale for a currency. VND's minor unit is the đồng.
int minorDigits(String currency) {
  switch (currency) {
    case 'VND':
    case 'JPY':
    case 'KRW':
      return 0;
    default:
      return 2;
  }
}

Exact _scale(String currency) => Exact.parse('1${'0' * minorDigits(currency)}');

/// [minor] units as a major amount. 150 USD cents is 1.5.
Exact majorAmount(int minor, String currency) => Exact.fromInt(minor) / _scale(currency);

/// A major amount rounded to the currency's minor unit.
int minorAmount(Exact major, String currency) => (major * _scale(currency)).roundToIntChecked();
