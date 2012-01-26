Local $result = myDiv(5, 0)
If @error Then
	MsgBox(4096, "Error", "Division by Zero")
Else
	MsgBox(4096, "Result", $result)
EndIf
Exit

Func myDiv($dividend, $divisor)
	If $dividend = 0 And $divisor = 0 Then
		SetError(2) ;indeterminate form 0/0
	ElseIf $divisor = 0 Then
		SetError(1) ;plain division by zero
	EndIf
	Return $dividend / $divisor
EndFunc   ;==>myDiv
