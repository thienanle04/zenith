import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/domain/money/currency.dart';
import 'package:zenith/domain/money/exact.dart';

/// The amount the owner typed, using the current separator pair.
Exact parseAmount(String input, SeparatorPair separators) {
  final grouping = separators == SeparatorPair.commaPoint ? ',' : '.';
  final decimal = separators == SeparatorPair.commaPoint ? '.' : ',';
  final normalized = input.trim().replaceAll(grouping, '').replaceAll(decimal, '.');
  return Exact.parse(normalized);
}

/// A major amount with grouping, without a plus or a minus.
String formatMajor(Exact amount, SeparatorPair separators) {
  final plain = amount.abs().formatPlain();
  final parts = plain.split('.');
  final grouped = _group(parts[0], separators == SeparatorPair.commaPoint ? ',' : '.');
  if (parts.length == 1) {
    return grouped;
  }
  final decimal = separators == SeparatorPair.commaPoint ? '.' : ',';
  return '$grouped$decimal${parts[1]}';
}

/// Money in [currency]. VND keeps đ against the number. Other signs lead with a space.
String formatMoney(Exact amount, String currency, SeparatorPair separators) {
  final number = formatMajor(amount, separators);
  if (currency == 'VND') {
    return '$numberđ';
  }
  final sign = switch (currency) {
    'USD' => r'$ ',
    'EUR' => '€ ',
    'GBP' => '£ ',
    'JPY' => '¥ ',
    _ => '$currency ',
  };
  return '$sign$number';
}

String formatMinor(int minor, String currency, SeparatorPair separators) => formatMoney(majorAmount(minor, currency), currency, separators);

String formatQuantity(Exact quantity, String unit, SeparatorPair separators) => '${formatMajor(quantity, separators)} $unit';

String _group(String digits, String separator) {
  final buffer = StringBuffer();
  for (var i = 0; i < digits.length; i++) {
    if (i > 0 && (digits.length - i) % 3 == 0) {
      buffer.write(separator);
    }
    buffer.write(digits[i]);
  }
  return buffer.toString();
}
