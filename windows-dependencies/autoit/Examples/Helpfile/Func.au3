Example1()
Example2()

; example1
Func Example1()
	; Sample script with three user-defined functions
	; Notice the use of variables, ByRef, and Return

	Local $foo = 2
	Local $bar = 5
	MsgBox(0, "Today is " & today(), "$foo equals " & $foo)
	swap($foo, $bar)
	MsgBox(0, "After swapping $foo and $bar", "$foo now contains " & $foo)
	MsgBox(0, "Finally", "The larger of 3 and 4 is " & max(3, 4))
EndFunc   ;==>Example1

Func swap(ByRef $a, ByRef $b) ;swap the contents of two variables
	Local $t
	$t = $a
	$a = $b
	$b = $t
EndFunc   ;==>swap

Func today() ;Return the current date in mm/dd/yyyy form
	Return (@MON & "/" & @MDAY & "/" & @YEAR)
EndFunc   ;==>today

Func max($x, $y) ;Return the larger of two numbers
	If $x > $y Then
		Return $x
	Else
		Return $y
	EndIf
EndFunc   ;==>max

;End of sample script 1

; example2
Func Example2()
	; Sample scriptusing @NumParams macro
	Test_Numparams(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
EndFunc   ;==>Example2

Func Test_Numparams($v1 = 0, $v2 = 0, $v3 = 0, $v4 = 0, $v5 = 0, $v6 = 0, $v7 = 0, $v8 = 0, $v9 = 0, _
		$v10 = 0, $v11 = 0, $v12 = 0, $v13 = 0, $v14 = 0, $v15 = 0, $v16 = 0, $v17 = 0, $v18 = 0, $v19 = 0)
	#forceref $v1, $v2, $v3, $v4, $v5, $v6, $v7, $v8, $v9, $v10, $v11, $v12, $v13, $v14, $v15, $v16, $v17, $v18, $v19
	Local $val
	For $i = 1 To @NumParams
		$val &= Eval("v" & $i) & " "
	Next
	MsgBox(0, "@NumParams example", "@NumParams =" & @NumParams & @CRLF & @CRLF & $val)
EndFunc   ;==>Test_Numparams

;End of sample script 2
