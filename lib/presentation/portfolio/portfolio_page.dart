import 'package:flutter/widgets.dart';
import 'package:forui/forui.dart';

import 'package:zenith/core/presentation/l10n/app_localizations.dart';

/// The portfolio, shown when the app opens.
class PortfolioPage extends StatelessWidget {
  const PortfolioPage({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = context.theme;

    return FScaffold(
      header: FHeader(title: Text(l10n.portfolio)),
      child: Text(
        l10n.portfolio,
        style: theme.typography.display.sm.copyWith(color: theme.colors.foreground),
      ),
    );
  }
}
