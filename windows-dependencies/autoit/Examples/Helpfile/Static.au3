; Static variables examples.

Func Test1()
	Static $STbFirstPass = 1

	If $STbFirstPass Then
		$STbFirstPass = 0
		; Perform tasks for the first time through
	EndIf
	; Other things the function should do
EndFunc   ;==>Test1

Func Accumulate($State)
	Static $Values[9]
	Local $I

	If IsNumber($State) Then
		Switch $State
			Case -1
				; Reset
				For $I = 0 To 8
					$Values[$I] = 0
				Next
				Return True
			Case -2
				Return $Values
			Case 0 To UBound($Values) - 1
				$Values[$State] += 1
				Return $Values[$State]
			Case Else
				If $State < 0 Then
					SetError(1, 0)
					Return False
				Else
					Static $Values[$State + 1] ; Resize the array to accomodate the new value
					$Values[$State] = 1
					Return 1
				EndIf
		EndSwitch
	Else
		SetError(2, 0)
	EndIf
EndFunc   ;==>Accumulate

Global $I

Test1()

For $I = 1 To 99
	Accumulate(Random(0, 20, 1))
Next
For $I In Accumulate(-2)
	ConsoleWrite($I & ", ")
Next
ConsoleWrite("\n");

Test1()
