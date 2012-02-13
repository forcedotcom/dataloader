Local $myArray[10][20] ;element 0,0 to 9,19
Local $rows = UBound($myArray)
Local $cols = UBound($myArray, 2)
Local $dims = UBound($myArray, 0)

MsgBox(0, "The " & $dims & "-dimensional array has", _
		$rows & " rows, " & $cols & " columns")

;Display $myArray's contents
Local $output = ""
For $r = 0 To UBound($myArray, 1) - 1
	$output = $output & @LF
	For $c = 0 To UBound($myArray, 2) - 1
		$output = $output & $myArray[$r][$c] & " "
	Next
Next
MsgBox(4096, "Array Contents", $output)
