Local $oShell = ObjCreate("shell.application")
If Not IsObj($oShell) Then
	MsgBox(0, "Error", "$oShell is not an Object.")
Else
	MsgBox(0, "Error", "Successfully created Object $oShell.")
EndIf
