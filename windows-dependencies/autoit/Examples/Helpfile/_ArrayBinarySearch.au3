#include <Array.au3>

;===============================================================================
; Example 1 (using a manually-defined array)
;===============================================================================
Local $avArray[10]

$avArray[0] = "JPM"
$avArray[1] = "Holger"
$avArray[2] = "Jon"
$avArray[3] = "Larry"
$avArray[4] = "Jeremy"
$avArray[5] = "Valik"
$avArray[6] = "Cyberslug"
$avArray[7] = "Nutster"
$avArray[8] = "JdeB"
$avArray[9] = "Tylo"

; sort the array to be able to do a binary search
_ArraySort($avArray)

; display sorted array
_ArrayDisplay($avArray, "$avArray AFTER _ArraySort()")

; lookup existing entry
Local $iKeyIndex = _ArrayBinarySearch($avArray, "Jon")
If Not @error Then
	MsgBox(0, 'Entry found', ' Index: ' & $iKeyIndex)
Else
	MsgBox(0, 'Entry Not found', ' Error: ' & @error)
EndIf

; lookup non-existing entry
$iKeyIndex = _ArrayBinarySearch($avArray, "Unknown")
If Not @error Then
	MsgBox(0, 'Entry found', ' Index: ' & $iKeyIndex)
Else
	MsgBox(0, 'Entry Not found', ' Error: ' & @error)
EndIf


;===============================================================================
; Example 2 (using an array returned by StringSplit())
;===============================================================================
$avArray = StringSplit("a,b,d,c,e,f,g,h,i", ",")

; sort the array to be able to do a binary search
_ArraySort($avArray, 0, 1) ; start at index 1 to skip $avArray[0]

; display sorted array
_ArrayDisplay($avArray, "$avArray AFTER _ArraySort()")

; start at index 1 to skip $avArray[0]
$iKeyIndex = _ArrayBinarySearch($avArray, "c", 1)
If Not @error Then
	MsgBox(0, 'Entry found', ' Index: ' & $iKeyIndex)
Else
	MsgBox(0, 'Entry Not found', ' Error: ' & @error)
EndIf
