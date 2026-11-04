import 'package:flutter/material.dart';
import 'package:forui/forui.dart';

import 'package:zenith/core/presentation/configs/app_config.dart';
import 'package:zenith/core/presentation/l10n/app_localizations.dart';
import 'package:zenith/core/presentation/routes/app_router.dart';
import 'package:zenith/core/presentation/theme/theme.dart';

/// Root widget. Screens sit under [FTheme] and never touch Drift.
class ZenithApp extends StatelessWidget {
  const ZenithApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: AppConfig.name,
      routerConfig: appRouter,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        ...FLocalizations.localizationsDelegates,
      ],
      supportedLocales: AppLocalizations.supportedLocales,
      theme: lightTheme.toApproximateMaterialTheme(),
      darkTheme: darkTheme.toApproximateMaterialTheme(),
      builder: (context, child) {
        final theme = Theme.of(context).brightness == Brightness.dark ? darkTheme : lightTheme;
        return FTheme(
          data: theme,
          child: FToaster(child: FTooltipGroup(child: child!)),
        );
      },
    );
  }
}
