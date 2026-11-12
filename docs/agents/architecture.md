# Architecture

How to read the current tree. Open this when changing modules, persistence, navigation, or platform source sets, or when an ADR disagrees with the tree. ADRs in `docs/adr/` are the decision record. When this file and an ADR disagree, this file is the tree.

## Modules

- `:domain` holds types, `PortfolioBook`, `PortfolioCommand`, and `PortfolioRepository`. It has no serialization and no SQLDelight.
- `:data` implements `PortfolioRepository`, encodes the JSON, and owns `DriverFactory` as expect/actual per platform. It depends on `:domain`.
- `:androidApp` is the only Jetpack Compose UI. It depends on `:domain` and `:data`.
- `domain` and `data` compile for Android, JVM, `iosArm64`, and `iosSimulatorArm64`. The iOS source sets keep shared code off Android-only APIs. There is no iOS UI.

## Wiring

`ZenithApplication` builds `SqlDelightPortfolioRepository`. One activity-scoped `PortfolioViewModel` loads the book, applies a command, and saves (ADR-0022).

## Store

One SQLDelight row, table `portfolio`, column `json` (ADR-0026).

## Packages

- Domain code lives in `com.zenith.domain.portfolio` and `com.zenith.domain.money`.
- Data code lives in `com.zenith.data`.
- Screen and shared-UI placement is in `docs/agents/coding.md`.
- Navigation keys are declared in `Destinations.kt`.

## Historical claims

These sentences in older ADRs are historical. The decision record stays in the ADR.

- ADR-0005: the store is Drift. The boundary in that ADR stays current: no account server, no second device, the phone keeps the book.
- ADR-0009: Flutter roots (`lib/domain`, `lib/core`, `lib/presentation`) and Drift in core.
- ADR-0019: the codec lives in `domain`. ADR-0026 moves it to `data`.
- ADR-0020: a hand-written codec in `domain`. ADR-0026 supersedes that. The one SQLDelight row stays current.
- ADR-0023: a back stack of five keys, and a hand-written book codec. The keys are declared in `Destinations.kt`.
- ADR-0025: grouping stays in the Android app. ADR-0028 groups accounts by kind.

## When the tree changes

A change that adds a module, moves persistence, adds a platform source set, or moves where navigation keys are declared updates this file in that same change. Done means this file matches the tree after the change.
