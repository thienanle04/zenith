# Amounts, rates, and quantities stay exact

An account amount is an integer in that currency's minor unit: đồng for VND, cents for USD. An exchange rate, an instrument price, and a holding quantity are exact decimals, so half a lượng and a rate of 25,450.5 survive unchanged. The domain does not use binary floating point.

Storing money as double would make the same đồng not equal to itself after arithmetic, and net worth would drift.
