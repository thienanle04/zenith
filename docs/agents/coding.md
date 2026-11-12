# Coding

How to write Kotlin in this repo. Open this when writing or reviewing Kotlin. ktlint, Detekt, and `.editorconfig` own format and analysis.

## UI

- New screens go in `com.zenith.app.features.<feature>`. Shared UI, navigation, theme, the writer, and l10n stay in `com.zenith.app.core`.
- A screen composable is PascalCase and ends in `Screen`. Add and edit for one entity share one file named for that entity.
- A screen preview is a private composable in that screen's file, with `showBackground` and `showSystemUi`.
- A component used by one feature lives in that feature's `components/`. A field or row used by more than one feature lives in `core/components`.
- A new route is a `@Serializable` `NavKey` named `*Destination` in `Destinations.kt`.
- Composable callbacks are `onDone`, `onSave`, and `onValue`.
- User-facing copy is an `R.string`. Domain types carry no display strings. A domain value maps to a string through `Labels.kt`.
- Inset and gap come from `MaterialTheme.spacing`. Strokes come from `MaterialTheme.thickness`.

## Book

- A book helper calls `refuse`. `caught` turns that into `PortfolioOutcome`. Code outside the book sees `PortfolioOutcome` only. ADR-0025 keeps the public refusal set.
- Account amounts stay with ADR-0007. Book-helper visibility stays with ADR-0029.

## Tests

- Book tests go in `domain`'s `commonTest`. Repository tests go in `data`'s `jvmTest`. ADR-0024 is why book tests run on the JVM.
- A test name is a camelCase sentence. Assertions come from `kotlin.test`.

## Comments

A type or field whose name is not enough gets one line of KDoc.

## When a habit changes

A change to one of these habits updates this file in that same change.
