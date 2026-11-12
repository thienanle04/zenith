# Form fields are an underline

A text field is an underline: a square bottom border, `#E5E5E5` at 1 dp at rest and the theme primary at 2 dp while focused. It draws no label. `AppTextRow` puts the label on the left and the field on the right.

A choice shows the label on the left and only the current choice as text on the right, in the theme primary. It draws no underline and no chevron. A tap opens the choices in a dialog at the center of the screen. `AppChoiceRow` is that row. A screen with no label uses `AppSelect` on its own.

Material's outlined field, with its box, radius, and select chevron, was the control these screens already used. The outlined box is the look these screens are leaving. A menu anchored to the field is the look the choice is leaving: the value is a short primary label, and the choices sit in the middle of the screen. Two row types keep a text row from accepting a select.
