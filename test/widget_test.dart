import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:zenith/app.dart';

void main() {
  testWidgets('opens on the portfolio', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: ZenithApp()));
    await tester.pumpAndSettle();

    expect(find.text('Portfolio'), findsWidgets);
  });
}
