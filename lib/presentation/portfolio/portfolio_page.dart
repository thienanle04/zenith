import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:go_router/go_router.dart';

import 'package:zenith/core/presentation/l10n/app_localizations.dart';
import 'package:zenith/core/presentation/routes/routes.dart';
import 'package:zenith/domain/book/portfolio_book.dart';
import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/presentation/portfolio/money_format.dart';
import 'package:zenith/presentation/providers/providers.dart';

const _positive = Color(0xFF1E88E5);
const _negative = Color(0xFFF55346);

/// The portfolio, shown when the app opens.
class PortfolioPage extends ConsumerWidget {
  const PortfolioPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final portfolio = ref.watch(portfolioProvider);

    return FScaffold(
      header: FHeader(title: Text(l10n.portfolio)),
      child: portfolio.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Text('$error'),
        data: (view) => _PortfolioBody(view: view),
      ),
    );
  }
}

class _PortfolioBody extends ConsumerWidget {
  const _PortfolioBody({required this.view});

  final PortfolioView view;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final theme = context.theme;

    return ListView(
      children: [
        Row(
          children: [
            _Figure(label: l10n.assets, amount: view.assets, separators: view.separators),
            _Figure(label: l10n.liabilities, amount: view.liabilities, separators: view.separators),
            _Figure(label: l10n.netWorth, amount: view.netWorth, separators: view.separators),
          ],
        ),
        const SizedBox(height: 16),
        for (var index = 0; index < view.sections.length; index++)
          _SectionTile(view: view, index: index, foreground: theme.colors.foreground),
        const SizedBox(height: 16),
        FButton(onPress: () => context.push(Routes.newAccount), child: Text(l10n.addAccount)),
        const SizedBox(height: 8),
        FButton(onPress: () => context.push(Routes.newHolding), child: Text(l10n.addHolding)),
      ],
    );
  }
}

class _Figure extends StatelessWidget {
  const _Figure({required this.label, required this.amount, required this.separators});

  final String label;
  final int amount;
  final SeparatorPair separators;

  @override
  Widget build(BuildContext context) {
    final theme = context.theme;
    final color = amount == 0 ? theme.colors.foreground : (amount > 0 ? _positive : _negative);
    return Expanded(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: theme.typography.body.xs.copyWith(color: theme.colors.mutedForeground)),
          Text(formatMinor(amount.abs(), 'VND', separators), style: theme.typography.display.xs.copyWith(color: color)),
        ],
      ),
    );
  }
}

class _SectionTile extends ConsumerWidget {
  const _SectionTile({required this.view, required this.index, required this.foreground});

  final PortfolioView view;
  final int index;
  final Color foreground;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final section = view.sections[index];
    final title = switch (section.kind) {
      SectionKind.assets => l10n.assets,
      SectionKind.liabilities => l10n.liabilities,
      SectionKind.physical => l10n.physicalHoldings,
      SectionKind.shares => section.heading ?? l10n.shares,
    };
    final notifier = ref.read(portfolioProvider.notifier);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          children: [
            Expanded(child: Text(title, style: context.theme.typography.display.xs)),
            FButton.icon(
              semanticsLabel: l10n.moveUp,
              onPress: index == 0 ? null : () => notifier.moveSection(section.key, -1),
              child: const Icon(Icons.keyboard_arrow_up),
            ),
            FButton.icon(
              semanticsLabel: l10n.moveDown,
              onPress: index == view.sections.length - 1 ? null : () => notifier.moveSection(section.key, 1),
              child: const Icon(Icons.keyboard_arrow_down),
            ),
          ],
        ),
        for (var rowIndex = 0; rowIndex < section.rows.length; rowIndex++)
          _RowTile(view: view, section: section, index: rowIndex, foreground: foreground),
      ],
    );
  }
}

class _RowTile extends ConsumerWidget {
  const _RowTile({required this.view, required this.section, required this.index, required this.foreground});

  final PortfolioView view;
  final PortfolioSection section;
  final int index;
  final Color foreground;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final row = section.rows[index];
    final notifier = ref.read(portfolioProvider.notifier);
    final (amount, currency) = switch (row.kind) {
      RowKind.account => (row.minor!, row.currency!),
      RowKind.holding => (null, row.valueCurrency!),
    };
    final text = row.kind == RowKind.account
        ? formatMinor(amount!, currency, view.separators)
        : formatMoney(row.value!, currency, view.separators);
    final sign = row.kind == RowKind.account ? amount!.sign : row.value!.sign;
    final color = sign == 0 ? foreground : (sign > 0 ? _positive : _negative);
    final subtitle = row.kind == RowKind.holding ? formatQuantity(row.quantity!, row.unit!, view.separators) : null;

    return GestureDetector(
      onTap: () => context.push(row.kind == RowKind.account ? Routes.account(row.id) : Routes.holding(row.id)),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(row.title),
                  if (subtitle != null)
                    Text(subtitle, style: context.theme.typography.body.xs.copyWith(color: context.theme.colors.mutedForeground)),
                ],
              ),
            ),
            Text(text, style: context.theme.typography.body.md.copyWith(color: color)),
            FButton.icon(
              semanticsLabel: l10n.moveUp,
              onPress: index == 0 ? null : () => notifier.moveRow(section.key, row.id, -1),
              child: const Icon(Icons.keyboard_arrow_up),
            ),
            FButton.icon(
              semanticsLabel: l10n.moveDown,
              onPress: index == section.rows.length - 1 ? null : () => notifier.moveRow(section.key, row.id, 1),
              child: const Icon(Icons.keyboard_arrow_down),
            ),
          ],
        ),
      ),
    );
  }
}
