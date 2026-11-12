# Navigation 3 owns the back stack

The Android app owns a back stack of five keys: portfolio, new account, account, new holding, and holding. Navigation 3 renders the top key. The portfolio `ViewModel` sits above that stack, so a pushed screen does not create a second writer. kotlinx.serialization is used for those keys only. The book codec stays hand-written.

Voyager is a stable back-stack library whose feature work has stopped, and it existed to share Compose navigation. Navigation 2 keeps the back stack inside a controller this app does not need. This phase has no shared UI, so the stack stays in the Android module.
