# An instrument's price currency is fixed

The owner names an instrument and chooses its unit and price currency at creation. Neither the unit nor the price currency can be changed later. A trade is allowed only when the cash account's currency matches that price currency at the time of the trade. Old trades stay in the currency they were stored in.

Allowing the price currency to change would leave past trades in one currency and the current price in another, with no single rate on the instrument to join them.
