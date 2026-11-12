# Amounts, rates, and quantities stay exact

An account amount stays exact. Its decimal digits follow that currency's transaction amount decimal precision, which starts at the usual minor unit: none for the đồng, two for the US cent. Saved amounts can keep digits beyond the precision now on screen. An exchange rate, an instrument price, and a holding quantity are exact decimals, so half a lượng and a rate of 25,450.5 survive unchanged. The domain does not use binary floating point. See ADR 0030.

Storing money as double would make the same đồng not equal to itself after arithmetic, and net worth would drift.
