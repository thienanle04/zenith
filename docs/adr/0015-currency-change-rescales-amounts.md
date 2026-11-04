# Changing an account's currency rescales its amounts

An account's balance is the sum of the amounts stored on it, so a new currency has to rescale those amounts. The factor starts from the rate list. Before saving, the owner may change the resulting balance or the rate, and every amount on that account, including its side of a transfer and the cash and fee of a trade, is multiplied by the same factor. The other account in a transfer is not rescaled. Holding quantities stay as they are. Each rescaled transaction keeps a VND rate so an unedited conversion still spends the same number of VND.

Leaving the old amounts in place would make a VND history add up to a dollar balance. Converting only the other leg of a transfer would rewrite an account the owner did not edit.
