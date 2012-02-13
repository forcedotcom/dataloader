; *****************************************************************************
; Example 1 - Declare a 1-dimensional array, and create an array showing the Possible Combinations
; *****************************************************************************

#include <Array.au3>

Local $aArray[5] = [1, 2, 3, 4, 5]

For $i = 1 To UBound($aArray)
	Local $aArrayCombo = _ArrayCombinations($aArray, $i, ",")
	_ArrayDisplay($aArrayCombo, "iSet = " & $i)
Next
