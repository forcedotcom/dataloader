#include <Array.au3>

Local $avArray[20]

; Populate test array.
For $i = 0 To UBound($avArray) - 1
	$avArray[$i] = Random(-20000, 20000, 1)
Next

_ArrayDisplay($avArray, "$avArray")

MsgBox(0, "_ArrayToString() getting $avArray items 1 to 7", _ArrayToString($avArray, @TAB, 1, 7))
