# Form fields are an underline

A text field and a select share one chrome: a square bottom border, `#E5E5E5` at 1 dp at rest and the theme primary at 2 dp while focused. The select shows its value as text and opens the existing menu on tap, with no chevron. Neither control draws a label. `AppTextRow` and `AppChoiceRow` put the label on the left and the control on the right. A screen with no label uses the control on its own.

Material's outlined field, with its box, radius, and select chevron, was the control these screens already used. One slot row could have held either control. The outlined box is the look these screens are leaving, and two row types keep a text row from accepting a select.
