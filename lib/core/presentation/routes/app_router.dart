import 'package:go_router/go_router.dart';

import 'package:zenith/core/presentation/routes/routes.dart';
import 'package:zenith/presentation/portfolio/portfolio_page.dart';

/// Binds [Routes] to screens.
final GoRouter appRouter = GoRouter(
  routes: [
    GoRoute(
      path: Routes.portfolio,
      builder: (context, state) => const PortfolioPage(),
    ),
  ],
);
