Local $result = StringCompare("MEL�N", "mel�n")
MsgBox(0, "StringCompare Result (mode 0):", $result)

$result = StringCompare("MEL�N", "mel�n", 1)
MsgBox(0, "StringCompare Result (mode 1):", $result)

$result = StringCompare("MEL�N", "mel�n", 2)
MsgBox(0, "StringCompare Result (mode 2):", $result)
