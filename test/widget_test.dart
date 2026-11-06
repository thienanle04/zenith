import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:forui/forui.dart';

import 'package:zenith/app.dart';
import 'package:zenith/core/presentation/theme/theme.dart';
import 'package:zenith/domain/book/book_repository.dart';
import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/presentation/providers/providers.dart';

void main() {
  testWidgets('opens on the portfolio', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [bookRepositoryProvider.overrideWithValue(_MemoryBook())],
        child: const ZenithApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Portfolio'), findsWidgets);
    expect(find.text('0đ'), findsNWidgets(3));

    await tester.tap(find.text('Add account'));
    await tester.pumpAndSettle();

    expect(find.text('Kind'), findsOneWidget);
    expect(tester.widget<Expanded>(find.ancestor(of: find.text('Kind'), matching: find.byType(Expanded)).first).flex, 1);
    expect(find.text('Cash'), findsOneWidget);
    expect(find.byType(FIcon), findsNothing);

    UnderlineInputBorder lineOf(
      InputDecorator decorator,
      Set<WidgetState> states,
    ) {
      final border = decorator.decoration.border;
      expect(border, isA<WidgetStateProperty<InputBorder>>());
      final resolved = (border! as WidgetStateProperty<InputBorder>).resolve(
        states,
      );
      expect(resolved, isA<UnderlineInputBorder>());
      return resolved! as UnderlineInputBorder;
    }

    final decorators = tester.widgetList<InputDecorator>(
      find.byType(InputDecorator),
    );
    expect(decorators, isNotEmpty);
    for (final decorator in decorators) {
      final line = lineOf(decorator, const {});
      expect(line.borderRadius, BorderRadius.zero);
      expect(line.borderSide.width, 1);
      expect(line.borderSide.color, lightTheme.colors.border);
      expect(decorator.decoration.fillColor, const Color(0x00000000));
    }

    final sample = decorators.first;
    final disabled = lightTheme.colors.disable(lightTheme.colors.border);
    expect(
      lineOf(sample, {WidgetState.error}).borderSide.color,
      lightTheme.colors.error,
    );
    expect(
      lineOf(sample, {WidgetState.error, WidgetState.focused}).borderSide.color,
      lightTheme.colors.error,
    );
    expect(lineOf(sample, {WidgetState.disabled}).borderSide.color, disabled);
    expect(
      lineOf(sample, {
        WidgetState.disabled,
        WidgetState.error,
      }).borderSide.color,
      disabled,
    );

    await tester.tap(find.byType(FTextField).first);
    await tester.pump();

    final focused = lineOf(
      tester.widget<InputDecorator>(find.byType(InputDecorator).first),
      {WidgetState.focused},
    );
    expect(focused.borderSide.color, lightTheme.colors.primary);
    expect(focused.borderSide.width, 1);
    expect(tester.takeException(), isNull);
  });
}

class _MemoryBook implements BookRepository {
  @override
  Future<BookSnapshot> load() async => BookSnapshot.empty;

  @override
  Future<void> save(BookSnapshot book) async {}
}
