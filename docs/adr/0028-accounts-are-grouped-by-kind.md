# Accounts are grouped by kind

The portfolio list used to place every account in Assets or Liabilities by the sign of its balance, so a bank at −500,000 sat with the loans. The sign already has a job: the Assets, Liabilities, and Net worth figures. Kind is where the owner looks for the account.

Accounts are grouped by kind: cash, bank, e-wallet, credit card, loan, receivable, and stock. A negative bank stays in Bank, and a positive credit card stays in Credit card. Changing kind moves the account and leaves the stored amounts as they are. Each group shows the signed VND sum of its own accounts. That sum is not added into net worth. The Stock sum is cash only.

Share holdings of one instrument are one section, totaled as a quantity, not as money. The same shares in two stock accounts stay two holdings. Physical holdings stay one section, totaled in VND. Net worth still values a holding as quantity times its current price.

A saved order under the Assets and Liabilities keys is dropped. Within a kind, accounts keep the order they had in Assets, then the order they had in Liabilities. Until a section is moved, sections take their default place: the kinds, then one share section per instrument, then physical holdings, and an empty one is left out. After a section has been moved, a section that appears later joins the end.
