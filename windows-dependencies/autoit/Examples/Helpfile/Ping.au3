Local $var = Ping("www.AutoItScript.com", 250)
If $var Then; also possible:  If @error = 0 Then ...
	MsgBox(0, "Status", "Online, roundtrip was:" & $var)
Else
	MsgBox(0, "Status", "An error occured with number: " & @error)
EndIf
