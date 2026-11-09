# Book tests run on the JVM

`domain` and `data` compile for Android, iOS, and the JVM. The JVM target is where `Exact`, the codec, and the book functions are tested on Windows and in the current GitHub workflow. The iOS targets stay in Gradle and compile on a Mac. The required check is the Android app.

A Mac-hosted iOS job would compile a target that has no product UI yet. The iOS source sets still keep the ledger from drifting into Android-only APIs.
