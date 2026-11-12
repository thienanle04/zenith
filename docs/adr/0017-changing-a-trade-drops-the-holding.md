# Changing a trade's type drops the holding

A trade can be changed into an income, an expense, or a transfer. The quantity and the separate fee are dropped. The cash magnitude, fee included, stays and takes the sign of the new type, so the account balance does not jump. The change is refused when the quantity would fall below zero. An income, an expense, or a transfer cannot become a trade. A balance adjustment is already an income or an expense, and it can change type the same way, which clears its system category.

Keeping the shares while relabeling the cash would leave a holding with no trade, and a quantity that no longer matches its movements.
