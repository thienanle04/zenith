/// An exact decimal. The domain never uses binary floating point.
final class Exact implements Comparable<Exact> {
  const Exact._(this._numerator, this._denominator);

  final BigInt _numerator;
  final BigInt _denominator;

  static final Exact zero = Exact._(BigInt.zero, BigInt.one);
  static final Exact one = Exact._(BigInt.one, BigInt.one);

  factory Exact.fromInt(int value) => Exact._(BigInt.from(value), BigInt.one);

  factory Exact.parse(String input) {
    final text = input.trim();
    if (text.isEmpty) {
      throw FormatException('empty amount', input);
    }
    var body = text;
    var negative = false;
    if (body.startsWith('-')) {
      negative = true;
      body = body.substring(1);
    } else if (body.startsWith('+')) {
      body = body.substring(1);
    }
    final parts = body.split('.');
    if (parts.length > 2 || parts.any((part) => part.isEmpty || !_digits.hasMatch(part))) {
      throw FormatException('invalid amount', input);
    }
    final scale = BigInt.from(10).pow(parts.length == 2 ? parts[1].length : 0);
    var numerator = BigInt.parse(parts[0]) * scale;
    if (parts.length == 2) {
      numerator += BigInt.parse(parts[1]);
    }
    if (negative) {
      numerator = -numerator;
    }
    return Exact._(numerator, scale)._reduced;
  }

  static final RegExp _digits = RegExp(r'^\d+$');

  bool get isZero => _numerator == BigInt.zero;

  bool get isNegative => _numerator < BigInt.zero;

  bool get isPositive => _numerator > BigInt.zero;

  int get sign => _numerator.sign;

  Exact operator +(Exact other) =>
      Exact._(_numerator * other._denominator + other._numerator * _denominator, _denominator * other._denominator)._reduced;

  Exact operator -() => Exact._(-_numerator, _denominator);

  Exact operator -(Exact other) => this + -other;

  Exact operator *(Exact other) => Exact._(_numerator * other._numerator, _denominator * other._denominator)._reduced;

  Exact operator /(Exact other) {
    if (other.isZero) {
      throw StateError('division by zero');
    }
    final denominator = _denominator * other._numerator.abs();
    var numerator = _numerator * other._denominator;
    if (other.isNegative) {
      numerator = -numerator;
    }
    return Exact._(numerator, denominator)._reduced;
  }

  /// Nearest integer, with halves away from zero.
  BigInt roundToInt() {
    final negative = isNegative;
    final numerator = _numerator.abs();
    var whole = numerator ~/ _denominator;
    final remainder = numerator.remainder(_denominator);
    if (remainder * BigInt.two >= _denominator) {
      whole += BigInt.one;
    }
    return negative ? -whole : whole;
  }

  int roundToIntChecked() {
    final rounded = roundToInt();
    if (rounded > BigInt.from(9007199254740991) || rounded < BigInt.from(-9007199254740991)) {
      throw StateError('amount does not fit');
    }
    return rounded.toInt();
  }

  Exact abs() => isNegative ? -this : this;

  /// Digits after the point. A repeating expansion stops after 24 digits.
  String formatPlain() {
    if (isZero) {
      return '0';
    }
    final negative = isNegative;
    final numerator = _numerator.abs();
    final whole = numerator ~/ _denominator;
    var remainder = numerator % _denominator;
    final digits = <String>[];
    while (remainder != BigInt.zero && digits.length < 24) {
      remainder *= BigInt.from(10);
      digits.add((remainder ~/ _denominator).toString());
      remainder = remainder % _denominator;
    }
    final body = digits.isEmpty ? '$whole' : '$whole.${digits.join()}';
    return negative ? '-$body' : body;
  }

  @override
  int compareTo(Exact other) => (_numerator * other._denominator).compareTo(other._numerator * _denominator);

  @override
  bool operator ==(Object other) => other is Exact && compareTo(other) == 0;

  @override
  int get hashCode => Object.hash(_reduced._numerator, _reduced._denominator);

  Exact get _reduced {
    if (_denominator == BigInt.one) {
      return this;
    }
    var divisor = _numerator.abs().gcd(_denominator);
    if (divisor == BigInt.zero) {
      divisor = BigInt.one;
    }
    return Exact._(_numerator ~/ divisor, _denominator ~/ divisor);
  }

  String toJson() => formatPlain();

  factory Exact.fromJson(String value) => Exact.parse(value);
}
