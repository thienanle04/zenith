# The book lives on the phone

v1 keeps the ledger in Drift on the device. Zenith has no account server and no sync to a second device. The camera and a bank connection are adapters: they may call an outside service, and they produce the same transactions the owner can type. The phone remains the only copy of the book.

A backend as the source of truth, with the phone as a cache, would add an account system before the ledger exists. A bank feed can still run as an adapter without becoming a second book.
