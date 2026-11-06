import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:go_router/go_router.dart';

import 'package:zenith/core/presentation/l10n/app_localizations.dart';
import 'package:zenith/core/presentation/utils/keyboard.dart';
import 'package:zenith/domain/book/portfolio_book.dart';
import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/domain/money/exact.dart';
import 'package:zenith/presentation/portfolio/labels.dart';
import 'package:zenith/presentation/portfolio/money_format.dart';
import 'package:zenith/presentation/providers/providers.dart';

class AddAccountPage extends ConsumerStatefulWidget {
  const AddAccountPage({super.key});

  @override
  ConsumerState<AddAccountPage> createState() => _AddAccountPageState();
}

class _AddAccountPageState extends ConsumerState<AddAccountPage> {
  final _name = TextEditingController();
  var _kind = AccountKind.cash;
  var _currency = 'VND';
  var _currencies = const ['VND'];
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final book = await ref.read(bookRepositoryProvider).load();
    if (!mounted) {
      return;
    }
    setState(() => _currencies = PortfolioBook.present(book).currencies);
  }

  @override
  void dispose() {
    _name.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    dismissKeyboard();
    try {
      await ref
          .read(portfolioProvider.notifier)
          .addAccount(name: _name.text, kind: _kind, currency: _currency);
      if (mounted) {
        context.pop();
      }
    } on BookFailure catch (error) {
      setState(() => _error = error.code);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return _EditorScaffold(
      title: l10n.addAccount,
      error: _error,
      onSave: _save,
      children: [
        _EditorRow(
          caption: l10n.name,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _name),
          ),
        ),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.kind,
          field: _KindField(
            kind: _kind,
            kinds: AccountKind.values,
            onChanged: (kind) => setState(() => _kind = kind),
          ),
        ),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.currency,
          field: _CurrencyField(
            currency: _currency,
            currencies: _currencies,
            onChanged: (currency) => setState(() => _currency = currency),
          ),
        ),
      ],
    );
  }
}

class AccountPage extends ConsumerStatefulWidget {
  const AccountPage({required this.id, super.key});

  final String id;

  @override
  ConsumerState<AccountPage> createState() => _AccountPageState();
}

class _AccountPageState extends ConsumerState<AccountPage> {
  final _name = TextEditingController();
  final _balance = TextEditingController();
  final _rate = TextEditingController();
  AccountEditor? _editor;
  late AccountKind _kind;
  late String _currency;
  String? _error;
  var _ready = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final book = await ref.read(bookRepositoryProvider).load();
    final editor = PortfolioBook.accountEditor(book, widget.id);
    _name.text = editor.name;
    _balance.text = formatMajor(editor.balance, editor.separators);
    _rate.text = formatMajor(editor.rate, editor.separators);
    _kind = editor.kind;
    _currency = editor.currency;
    if (!mounted) {
      return;
    }
    setState(() {
      _editor = editor;
      _ready = true;
    });
  }

  @override
  void dispose() {
    _name.dispose();
    _balance.dispose();
    _rate.dispose();
    super.dispose();
  }

  Future<void> _onCurrency(String currency) async {
    final editor = _editor;
    if (editor == null) {
      return;
    }
    final book = await ref.read(bookRepositoryProvider).load();
    final rate = currency == 'VND'
        ? Exact.one
        : book.rates[currency] ?? Exact.one;
    final proposed = currency == editor.currency
        ? editor.balance
        : PortfolioBook.proposedBalance(book, editor.id, currency, rate);
    setState(() {
      _currency = currency;
      _rate.text = formatMajor(rate, editor.separators);
      _balance.text = formatMajor(proposed, editor.separators);
    });
  }

  Future<void> _save() async {
    dismissKeyboard();
    final editor = _editor;
    if (editor == null) {
      return;
    }
    try {
      await ref
          .read(portfolioProvider.notifier)
          .saveAccount(
            id: editor.id,
            name: _name.text,
            kind: _kind,
            currency: _currency,
            balance: parseAmount(_balance.text, editor.separators),
            rate: _currency == editor.currency
                ? null
                : parseAmount(_rate.text, editor.separators),
          );
      if (mounted) {
        context.pop();
      }
    } on BookFailure catch (error) {
      setState(() => _error = error.code);
    } on FormatException {
      setState(() => _error = 'invalidAmount');
    }
  }

  Future<void> _setAside() async {
    dismissKeyboard();
    await ref.read(portfolioProvider.notifier).setAside(widget.id);
    if (mounted) {
      context.pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    if (!_ready || _editor == null) {
      return const FScaffold(child: Center(child: CircularProgressIndicator()));
    }
    final editor = _editor!;
    final kinds = editor.kind == AccountKind.stock
        ? const [AccountKind.stock]
        : [
            for (final kind in AccountKind.values)
              if (kind != AccountKind.stock) kind,
          ];
    return _EditorScaffold(
      title: editor.name,
      error: _error,
      onSave: _save,
      children: [
        _EditorRow(
          caption: l10n.name,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _name),
          ),
        ),
        const SizedBox(height: 10),
        _EditorRow(
          caption: l10n.kind,
          enabled: editor.kind != AccountKind.stock,
          field: _KindField(
            kind: _kind,
            kinds: kinds,
            onChanged: editor.kind == AccountKind.stock
                ? null
                : (kind) => setState(() => _kind = kind),
          ),
        ),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.currency,
          field: _CurrencyField(
            currency: _currency,
            currencies: editor.currencies,
            onChanged: _onCurrency,
          ),
        ),
        if (_currency != editor.currency) ...[
          const SizedBox(height: 12),
          _EditorRow(
            caption: l10n.rate,
            field: FTextField(
              control: FTextFieldControl.managed(controller: _rate),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
            ),
          ),
        ],
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.balance,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _balance),
            keyboardType: const TextInputType.numberWithOptions(
              decimal: true,
              signed: true,
            ),
          ),
        ),
        const SizedBox(height: 12),
        FButton(
          variant: .destructive,
          onPress: _setAside,
          child: Text(l10n.setAside),
        ),
      ],
    );
  }
}

class _KindField extends StatelessWidget {
  const _KindField({
    required this.kind,
    required this.kinds,
    required this.onChanged,
  });

  final AccountKind kind;
  final List<AccountKind> kinds;
  final ValueChanged<AccountKind>? onChanged;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return FSelect<AccountKind>(
      enabled: onChanged != null,
      suffixBuilder: null,
      control: FSelectControl.lifted(
        value: kind,
        onChange: (value) {
          if (value != null) {
            onChanged?.call(value);
          }
        },
      ),
      items: {for (final item in kinds) accountKindLabel(l10n, item): item},
    );
  }
}

class _CurrencyField extends StatelessWidget {
  const _CurrencyField({
    required this.currency,
    required this.currencies,
    required this.onChanged,
  });

  final String currency;
  final List<String> currencies;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    return FSelect<String>(
      suffixBuilder: null,
      control: FSelectControl.lifted(
        value: currency,
        onChange: (value) {
          if (value != null) {
            onChanged(value);
          }
        },
      ),
      items: {for (final item in currencies) item: item},
    );
  }
}

class _EditorRow extends StatelessWidget {
  const _EditorRow({
    required this.caption,
    required this.field,
    this.enabled = true,
  });

  final String caption;
  final Widget field;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    final colors = context.theme.colors;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(
          flex: 1,
          child: Text(
            caption,
            style: context.theme.typography.body.xs.copyWith(
              fontWeight: FontWeight.w500,
              color: enabled
                  ? colors.foreground
                  : colors.disable(colors.foreground),
            ),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(flex: 5, child: field),
      ],
    );
  }
}

class _EditorScaffold extends StatelessWidget {
  const _EditorScaffold({
    required this.title,
    required this.children,
    required this.onSave,
    this.error,
  });

  final String title;
  final List<Widget> children;
  final VoidCallback onSave;
  final String? error;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return FScaffold(
      header: FHeader.nested(
        title: Text(title),
        prefixes: [
          FHeaderAction(
            icon: const Icon(Icons.arrow_back),
            onPress: () => context.pop(),
          ),
        ],
      ),
      child: ListView(
        children: [
          ...children,
          if (error != null) ...[
            const SizedBox(height: 12),
            Text(bookFailureMessage(l10n, error!)),
          ],
          const SizedBox(height: 16),
          FButton(onPress: onSave, child: Text(l10n.save)),
        ],
      ),
    );
  }
}
