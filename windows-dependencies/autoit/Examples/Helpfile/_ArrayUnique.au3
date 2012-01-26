; *****************************************************************************
; Example 1 - Declare a 1-dimensional array, that contains duplicate values.
; Use _ArrayUnique to Create a New Array that only contains the unique values.
; *****************************************************************************

#include <Array.au3>

Local $aArray[10] = [1, 2, 3, 4, 5, 1, 2, 3, 4, 5]
_ArrayDisplay($aArray, "$aArray")
Local $aNewArray = _ArrayUnique($aArray) ;Using Default Parameters
_ArrayDisplay($aNewArray, "$aNewArray represents the 1st Dimension of $aArray")

; ******************************************************************************************
; Example 2 - Declare a 2-dimensional array, that contains duplicate values.
; Use _ArrayUnique to Create a New 1-dimensional Array that only contains the unique values.
; ******************************************************************************************

#include <Array.au3>

Dim $aArray[6][2] = [[1, "A"],[2, "B"],[3, "C"],[1, "A"],[2, "B"],[3, "C"]]
_ArrayDisplay($aArray, "$aArray")
$aNewArray = _ArrayUnique($aArray) ;Using Default Parameters
_ArrayDisplay($aNewArray, "$aNewArray represents the 1st Dimension of $aArray")

$aNewArray = _ArrayUnique($aArray, 2) ;Using 2nd Dimension
_ArrayDisplay($aNewArray, "$aNewArray represents the 2nd Dimension of $aArray")

; *****************************************************************************************
; Example 3 - Declare a 1-dimensional array, that contains duplicate values.
; Use _ArrayUnique and case sensitivity to Create a New Array, with only the unique values.
; *****************************************************************************************

#include <Array.au3>

Dim $aArray[6][2] = [[1, "A"],[2, "B"],[3, "C"],[1, "a"],[2, "b"],[3, "c"]]
_ArrayDisplay($aArray, "$aArray")
$aNewArray = _ArrayUnique($aArray, 1, 0, 1) ;Using Default Parameters, with Case-Sensitivity
_ArrayDisplay($aNewArray, "$aNewArray represents the 1st Dimension of $aArray")

$aNewArray = _ArrayUnique($aArray, 2, 0, 1) ;Using Default Parameters, with Case-Sensitivity
_ArrayDisplay($aNewArray, "$aNewArray represents the 2st Dimension of $aArray")

; *****************************************************************************************
; Example 4 - Declare a 1-dimensional array, that contains duplicate values and "|".
; Use _ArrayUnique to Create a New Array, with only the unique values.
; *****************************************************************************************

#include <Array.au3>

Dim $aArray[6][2] = [[1, "|A"],[2, "B"],[3, "C"],[1, "|A"],[2, "B"],[3, "C"]]
Local $sMsgBox

$aNewArray = _ArrayUnique($aArray, 2) ;Using 2nd-Dimension

For $i = 0 To $aNewArray[0]
	$sMsgBox &= "[" & $i & "]: " & $aNewArray[$i] & @CRLF
Next

;Must change paramaters to show an element containing "|" in _ArrayDisplay
_ArrayDisplay($aNewArray, "$aNewArray represents the 1st Dimension of $aArray", -1, 0, "@")
