# Accounts, holdings, and payment methods are different things

Zenith tracks spendable balances and priced holdings in one portfolio. Treating cash, cards, loans, stocks, and gold as one kind of account collapses things that do not share a balance.

An account is a single-currency balance: an asset (cash, bank, e-wallet, receivable) or a liability (credit card, loan). A debit card is a payment method on an account and has no balance. A holding is a quantity of an instrument, valued as quantity times price, so its unit need not be a currency.

The alternative was one account type with a kind flag covering cards, stocks, and gold. That flag would make every transfer, bill, and net-worth total special-case a debit card with no balance and a gold bar denominated in lượng.
