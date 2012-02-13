; Create the binary data 0x10203040
Local $binary = Binary("0x10203040")
Local $extract = BinaryMid($binary, 2, 2)
MsgBox(0, "2nd and 3rd bytes are", $extract)
