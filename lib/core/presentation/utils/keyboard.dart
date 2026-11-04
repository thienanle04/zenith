import 'package:flutter/widgets.dart';

/// Drops the current focus so an open keyboard closes.
void dismissKeyboard() {
  FocusManager.instance.primaryFocus?.unfocus();
}
