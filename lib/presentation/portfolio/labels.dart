import 'package:zenith/core/presentation/l10n/app_localizations.dart';
import 'package:zenith/domain/book/snapshot.dart';

String accountKindLabel(AppLocalizations l10n, AccountKind kind) => switch (kind) {
  AccountKind.cash => l10n.cash,
  AccountKind.bank => l10n.bank,
  AccountKind.eWallet => l10n.eWallet,
  AccountKind.creditCard => l10n.creditCard,
  AccountKind.loan => l10n.loan,
  AccountKind.receivable => l10n.receivable,
  AccountKind.stock => l10n.stock,
};

String bookFailureMessage(AppLocalizations l10n, String code) => switch (code) {
  'nameRequired' => l10n.nameRequired,
  'currencyNeedsRate' => l10n.currencyNeedsRate,
  'stockKindFixed' => l10n.stockKindFixed,
  'stockAccountRequired' => l10n.stockAccountRequired,
  'ratePositive' => l10n.ratePositive,
  'notFound' => l10n.notFound,
  'invalidAmount' => l10n.invalidAmount,
  _ => code,
};
