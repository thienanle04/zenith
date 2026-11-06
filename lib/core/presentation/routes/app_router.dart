import 'package:go_router/go_router.dart';

import 'package:zenith/core/presentation/routes/routes.dart';
import 'package:zenith/presentation/portfolio/account_page.dart';
import 'package:zenith/presentation/portfolio/holding_page.dart';
import 'package:zenith/presentation/portfolio/portfolio_page.dart';

/// Binds [Routes] to screens.
final GoRouter appRouter = GoRouter(
  routes: [
    GoRoute(path: Routes.portfolio, builder: (context, state) => const PortfolioPage()),
    GoRoute(path: Routes.newAccount, builder: (context, state) => const AddAccountPage()),
    GoRoute(path: '/accounts/:id', builder: (context, state) => AccountPage(id: state.pathParameters['id']!)),
    GoRoute(path: Routes.newHolding, builder: (context, state) => const AddHoldingPage()),
    GoRoute(path: '/holdings/:id', builder: (context, state) => HoldingPage(id: state.pathParameters['id']!)),
  ],
);
