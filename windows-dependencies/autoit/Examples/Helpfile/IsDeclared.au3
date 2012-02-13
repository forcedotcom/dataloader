If Not IsDeclared("a") Then
	MsgBox(0, "", "$a is NOT declared") ; $a has never been assigned
EndIf

Local $a = 1

If IsDeclared("a") Then
	MsgBox(0, "", "$a IS declared") ; due to previous $a=1 assignment
EndIf
