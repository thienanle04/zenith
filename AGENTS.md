## About

Zenith is one owner's portfolio on their Android phone. The phone keeps the book: one owner, no account server. Names for accounts, holdings, movements, and worth are in `GLOSSARY.md`.

`domain` and `data` are Kotlin Multiplatform; the only UI is Jetpack Compose in `androidApp`; the book is one SQLDelight JSON document on the phone.

## Architecture

Open `docs/agents/architecture.md` when changing modules, persistence, navigation, or platform source sets, or when an ADR disagrees with the tree.

## Coding

Open `docs/agents/coding.md` when writing or reviewing Kotlin.

## Agent skills

### Issue tracker

Issues live as markdown files under `.scratch/<feature>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: root `GLOSSARY.md` + `docs/adr/`. See `docs/agents/domain.md`.
