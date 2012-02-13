Local $text = StringReplace("this is a line of text", " ", "-")
Local $numreplacements = @extended
MsgBox(0, "New string is", $text)
MsgBox(0, "The number of replacements done was", $numreplacements)
