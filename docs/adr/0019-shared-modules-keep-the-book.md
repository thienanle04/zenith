# Shared modules keep the book, Compose stays in the Android app

ADR-0026 moves the codec from `domain` to `data`.

Zenith’s Kotlin Multiplatform cut is the book, not the screens. `domain` holds types, the codec, and the book functions, and it does not depend on the document store. `data` loads and saves the one document and implements the repository. Both compile for Android and iOS. The Android app is the only Jetpack Compose module in this phase. There is no iOS UI.

Compose Multiplatform would put navigation and lifecycle in shared code before an iOS UI exists. An Android-only module would drop the iOS target and leave the ledger unchecked as multiplatform code.
