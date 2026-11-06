/// Route paths. The router that binds them to screens lives beside this file.
abstract final class Routes {
  static const String portfolio = '/';
  static const String newAccount = '/accounts/new';
  static const String newHolding = '/holdings/new';

  static String account(String id) => '/accounts/$id';

  static String holding(String id) => '/holdings/$id';
}
