# The book is one JSON document in SQLDelight

ADR-0026 supersedes the hand-written codec and the refusal of a serialization library. The portfolio remains one SQLDelight row.

`data` stores the book as a single SQLDelight row whose column is a JSON document. `domain` encodes and decodes that document with a hand-written codec, the same shape as `BookCodec`, and it does not take a serialization library. Android and iOS each supply a SQLDelight driver.

A file would drop the atomic replace the phone already has for its only copy of the book. kotlinx.serialization would move the trade, rate, and exact-amount rules into generated serializers. Room would put an AndroidX database in the shared module without matching the current one-document table any more closely.
