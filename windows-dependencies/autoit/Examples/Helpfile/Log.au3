Local $x = Log(1000) ;returns 6.90775527898214

Local $y = Log10(1000) ;returns 3

; user-defined function for common log
Func Log10($x)
	Return Log($x) / Log(10) ;10 is the base
EndFunc   ;==>Log10
