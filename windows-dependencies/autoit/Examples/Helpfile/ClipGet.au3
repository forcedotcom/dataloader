Local $bak = ClipGet()
MsgBox(0, "Clipboard contains:", $bak)

ClipPut($bak & "additional text")
MsgBox(0, "Clipboard contains:", ClipGet())
