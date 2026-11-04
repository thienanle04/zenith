# Zenith

One owner's portfolio: where money moved, and what that portfolio is worth.

## Book

**Portfolio**:
The single book belonging to the one owner.
_Avoid_: Household, book, wallet

**Net worth**:
The sum of the portfolio's account balances and the value of its holdings, as of now. A liability balance is negative, so it reduces that sum. Prices and the rate list keep no history.

**Reporting currency**:
The currency net worth is expressed in. It is VND.

## Balances

**Account**:
A named balance in one currency. Cash, a bank deposit, and an e-wallet are asset accounts. A credit card is a liability account. A liability balance is negative while the owner owes money. Changing kind among cash, bank, e-wallet, credit card, loan, and receivable leaves every stored amount as it is. Changing its currency rescales every amount stored on it by one factor. That factor starts from the rate list, and the owner may change the resulting balance or the rate before saving. The other account in a transfer is left as it is.
_Avoid_: Wallet, card

**Loan**:
A liability account for money the owner owes a lender.
_Avoid_: Debt

**Receivable**:
An asset account for money someone owes the owner.
_Avoid_: Debt

**Payment method**:
A way to spend from an asset account, such as a debit card. It has no balance of its own. An expense may record which payment method was used. A credit-card swipe records no payment method, because the credit card is the account.
_Avoid_: Debit card

## Movements

**Balance adjustment**:
An income or an expense created when the owner types an account's balance other than the one it already shows. The amount stored is the signed difference. Positive uses the modify-balance income category. Negative uses the modify-balance expense category. It can change type the same way as any other income or expense, and that change clears the system category. Typing zero on a new account creates no transaction.
_Avoid_: Opening balance

**Exchange rate**:
The VND value of one unit of a transaction's currency, stored on that transaction. The owner may change it while creating the transaction and by editing it later. Spending totals use this stored rate.
_Avoid_: Live rate, market rate

**Rate list**:
The owner's exchange rate into VND for each currency, one row each. A new transaction starts from this list, and net worth converts foreign account balances and foreign prices through it. A currency is added to the list before an account or a price in that currency can be saved.

**Category**:
A label the owner creates, either for incomes or for expenses. Only these are shown, and the owner chooses from them. The owner may rename one. Its side stays as created. Deleting one clears it on existing transactions, which become uncategorized. Changing an income, an expense, or a transfer into one of the other two clears the category.
_Avoid_: a label used for both income and expense

**System category**:
The modify-balance income category, the modify-balance expense category, or the fee category. The owner never sees these and cannot rename, delete, or choose them.
_Avoid_: uncategorized

**Uncategorized**:
An income or an expense with no category.
_Avoid_: a built-in uncategorized category

**Income**:
A transaction that increases net worth on one account. Its amount is positive. It may carry an income category.

**Expense**:
A transaction that decreases net worth on one account. Its amount is negative. It may carry an expense category and a payment method. Changing it into an income keeps the magnitude and makes the amount positive. Changing an income into an expense keeps the magnitude and makes the amount negative.

**Bank reference**:
The identifier a bank assigns to one of its rows. A transaction created from that row stores the reference and the row's signed amount as-is, on a credit card as well as on a bank account. Importing the same reference again adds nothing.

**Occurred at**:
The local date and time of a transaction. Income and expense totals for a period include a transaction when this time falls in the period.

**Note**:
The words shown for a transaction in the list. A bank row fills the note with the bank's text. A photo fills it with the merchant, and fills the time from the bill, or with the current time when the bill has none.
_Avoid_: description

**Description**:
Optional extra words on a transaction. A bank row and a photo leave it empty.
_Avoid_: note

**Transfer**:
A movement of money between two of the owner's accounts. It has no category, and net worth does not change.

**Trade**:
One record that changes a holding's quantity and the cash paid or received, fee included. The instrument's price currency is that cash account's currency. For shares, the cash account is the stock account the holding belongs to. For physical gold, it is the account that paid. Removing the trade removes the cash, the quantity, and the fee together. Changing a trade into an income, an expense, or a transfer drops the quantity and the separate fee, and the cash magnitude, fee included, follows that new type. That change is refused when the holding's quantity would fall below zero. An income, an expense, or a transfer cannot become a trade. The cash amount on the trade is history. Net worth uses the holding's quantity times its instrument's current price, and the sale's profit is not income.
_Avoid_: paying for shares straight from a bank account

**Holding adjustment**:
A movement that changes a holding's quantity and does not move cash. It has no category. Typing the quantity the holding already shows records nothing.

## Holdings

**Instrument**:
A thing the owner creates, with a name, a unit such as a share or a lượng, and a price in one currency. The unit and the price currency do not change after creation. A stock and physical gold are instruments.

**Stock account**:
An asset account whose cash is used to buy and sell shares. The portfolio can contain more than one. Its kind does not change. Cash is moved in by a transfer before a purchase. A purchase that would make this cash negative is refused. Sale proceeds, and income such as a dividend, land in this same account.
_Avoid_: brokerage

**Holding**:
A quantity of one instrument, equal to the sum of its trades and holding adjustments. A share holding belongs to one stock account, so the same shares in two stock accounts are two holdings. A physical holding, such as gold in hand, belongs to the portfolio and not to a stock account.
_Avoid_: Position, account

**Price**:
The current value of one unit of an instrument, in one currency, as typed by the owner. Every holding of that instrument uses the same price.
_Avoid_: Quote, market price
