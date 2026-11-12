# The portfolio writer is one Android ViewModel

The Android app has one activity-scoped `ViewModel`. It loads the book, runs a pure book function, saves, and holds the presented portfolio in a `StateFlow`. Editors keep field state and pass a failure back to the screen. The repository stays load and save.

Orbit would wrap that writer in a container and a side-effect channel, and its saved-state path is `SavedStateHandle`, which is the wrong place for the whole portfolio. The book is already the SQLDelight document.
