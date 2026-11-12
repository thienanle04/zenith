# The portfolio command takes typed text and returns a failure

The portfolio module accepts one command. Amounts on that command are the text the owner typed, parsed with the separators the portfolio already stores. A refusal is one of a closed set, returned by the command, the currency preview, and the two editors. The writer remains the one ViewModel from ADR-0022: it loads, applies that command, saves, and presents. Screens display the failure. Grouping and currency signs stay in the Android app.

A writer method per command would leave the interface as wide as the implementation. Parsing in the screens would leave an unreadable amount as an exception the portfolio module never sees. Throwing the failure would make the tests catch an exception instead of reading the result the screens read.
