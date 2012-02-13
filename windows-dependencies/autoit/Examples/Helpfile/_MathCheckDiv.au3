#include <Math.au3>

Local $I_Var = InputBox('Odd or Even', 'Enter a number:')
Local $I_Result = _MathCheckDiv($I_Var, 2)
If $I_Result = -1 Or @error = 1 Then
	MsgBox(0, '', 'You did not enter a valid number')
ElseIf $I_Result = 1 Then
	MsgBox(0, '', 'Number is odd')
ElseIf $I_Result = 2 Then
	MsgBox(0, '', 'Number is even')
Else
	MsgBox(0, '', 'Could not parse $I_Result')
EndIf
