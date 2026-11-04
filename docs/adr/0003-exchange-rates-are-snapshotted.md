# A transaction keeps its own exchange rate

Spending totals have to stay still when today's rate changes. Each transaction stores how many VND one unit of its currency was worth. That rate starts from the rate list, and the owner can change it while creating the transaction or by editing it later. A later change to the rate list does not rewrite transactions already saved. A VND transaction stores a rate of 1.

Net worth converts a foreign account balance, and a price quoted in a foreign currency, at the rate list. Spending totals do not.

The alternative was converting every past movement at the current rate. Historical income and expense in VND would move every time the rate list was edited.
