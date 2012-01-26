Local $text = ""
For $i = 256 To 512
	$text = $text & ChrW($i)
Next
MsgBox(0, "Unicode chars 256 to 512", $text)
