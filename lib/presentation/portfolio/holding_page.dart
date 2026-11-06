import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:forui/forui.dart';
import 'package:go_router/go_router.dart';

import 'package:zenith/core/presentation/l10n/app_localizations.dart';
import 'package:zenith/core/presentation/utils/keyboard.dart';
import 'package:zenith/domain/book/portfolio_book.dart';
import 'package:zenith/domain/book/snapshot.dart';
import 'package:zenith/presentation/portfolio/labels.dart';
import 'package:zenith/presentation/portfolio/money_format.dart';
import 'package:zenith/presentation/providers/providers.dart';

class AddHoldingPage extends ConsumerStatefulWidget {
  const AddHoldingPage({super.key});

  @override
  ConsumerState<AddHoldingPage> createState() => _AddHoldingPageState();
}

class _AddHoldingPageState extends ConsumerState<AddHoldingPage> {
  final _name = TextEditingController();
  final _unit = TextEditingController();
  final _price = TextEditingController();
  final _quantity = TextEditingController(text: '0');
  var _currency = 'VND';
  var _currencies = const ['VND'];
  var _separators = SeparatorPair.commaPoint;
  var _shares = false;
  List<StockAccountChoice> _stockAccounts = const [];
  String? _stockAccountId;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final book = await ref.read(bookRepositoryProvider).load();
    final view = PortfolioBook.present(book);
    if (!mounted) {
      return;
    }
    setState(() {
      _currencies = view.currencies;
      _separators = view.separators;
      _stockAccounts = view.stockAccounts;
      _stockAccountId = view.stockAccounts.isEmpty
          ? null
          : view.stockAccounts.first.id;
    });
  }

  @override
  void dispose() {
    _name.dispose();
    _unit.dispose();
    _price.dispose();
    _quantity.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    dismissKeyboard();
    if (_shares && _stockAccountId == null) {
      setState(() => _error = 'stockAccountRequired');
      return;
    }
    try {
      await ref
          .read(portfolioProvider.notifier)
          .addInstrument(
            name: _name.text,
            unit: _unit.text,
            priceCurrency: _currency,
            price: parseAmount(_price.text, _separators),
            quantity: parseAmount(_quantity.text, _separators),
            stockAccountId: _shares ? _stockAccountId : null,
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

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return _HoldingScaffold(
      title: l10n.addHolding,
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
          caption: l10n.unit,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _unit),
          ),
        ),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.currency,
          field: FSelect<String>(
            suffixBuilder: null,
            control: FSelectControl.lifted(
              value: _currency,
              onChange: (value) {
                if (value != null) {
                  setState(() => _currency = value);
                }
              },
            ),
            items: {for (final currency in _currencies) currency: currency},
          ),
        ),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.price,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _price),
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
          ),
        ),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.quantity,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _quantity),
            keyboardType: const TextInputType.numberWithOptions(
              decimal: true,
              signed: true,
            ),
          ),
        ),
        const SizedBox(height: 12),
        FSwitch(
          label: Text(l10n.shares),
          value: _shares,
          onChange: (value) => setState(() => _shares = value),
        ),
        if (_shares && _stockAccounts.isNotEmpty) ...[
          const SizedBox(height: 12),
          _EditorRow(
            caption: l10n.stock,
            field: FSelect<String>.rich(
              suffixBuilder: null,
              format: (id) =>
                  _stockAccounts.firstWhere((account) => account.id == id).name,
              control: FSelectControl.lifted(
                value: _stockAccountId,
                onChange: (value) => setState(() => _stockAccountId = value),
              ),
              children: [
                for (final account in _stockAccounts)
                  FSelectItem(title: Text(account.name), value: account.id),
              ],
            ),
          ),
        ],
      ],
    );
  }
}

class HoldingPage extends ConsumerStatefulWidget {
  const HoldingPage({required this.id, super.key});

  final String id;

  @override
  ConsumerState<HoldingPage> createState() => _HoldingPageState();
}

class _HoldingPageState extends ConsumerState<HoldingPage> {
  final _name = TextEditingController();
  final _price = TextEditingController();
  final _quantity = TextEditingController();
  HoldingEditor? _editor;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final book = await ref.read(bookRepositoryProvider).load();
    final editor = PortfolioBook.holdingEditor(book, widget.id);
    _name.text = editor.name;
    _price.text = formatMajor(editor.price, editor.separators);
    _quantity.text = formatMajor(editor.quantity, editor.separators);
    if (!mounted) {
      return;
    }
    setState(() => _editor = editor);
  }

  @override
  void dispose() {
    _name.dispose();
    _price.dispose();
    _quantity.dispose();
    super.dispose();
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
          .saveHolding(
            holdingId: editor.holdingId,
            name: _name.text,
            price: parseAmount(_price.text, editor.separators),
            quantity: parseAmount(_quantity.text, editor.separators),
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

  Future<void> _removeHolding() async {
    await ref.read(portfolioProvider.notifier).removeHolding(widget.id);
    if (mounted) {
      context.pop();
    }
  }

  Future<void> _removeInstrument() async {
    final editor = _editor;
    if (editor == null) {
      return;
    }
    await ref
        .read(portfolioProvider.notifier)
        .removeInstrument(editor.instrumentId);
    if (mounted) {
      context.pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final editor = _editor;
    if (editor == null) {
      return const FScaffold(child: Center(child: CircularProgressIndicator()));
    }
    return _HoldingScaffold(
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
        const SizedBox(height: 12),
        Text('${l10n.unit}: ${editor.unit}'),
        Text('${l10n.currency}: ${editor.priceCurrency}'),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.price,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _price),
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
          ),
        ),
        const SizedBox(height: 12),
        _EditorRow(
          caption: l10n.quantity,
          field: FTextField(
            control: FTextFieldControl.managed(controller: _quantity),
            keyboardType: const TextInputType.numberWithOptions(
              decimal: true,
              signed: true,
            ),
          ),
        ),
        const SizedBox(height: 12),
        FButton(
          variant: .destructive,
          onPress: _removeHolding,
          child: Text(l10n.removeHolding),
        ),
        const SizedBox(height: 8),
        FButton(
          variant: .destructive,
          onPress: _removeInstrument,
          child: Text(l10n.removeInstrument),
        ),
      ],
    );
  }
}

class _EditorRow extends StatelessWidget {
  const _EditorRow({required this.caption, required this.field});

  final String caption;
  final Widget field;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(
          flex: 1,
          child: Text(
            caption,
            style: context.theme.typography.body.xs.copyWith(
              fontWeight: FontWeight.w500,
              color: context.theme.colors.foreground,
            ),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(flex: 5, child: field),
      ],
    );
  }
}

class _HoldingScaffold extends StatelessWidget {
  const _HoldingScaffold({
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
