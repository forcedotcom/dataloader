#include <Array.au3>	; For _ArrayDisplay()

; Convert the string to an array.
Local $a = StringToASCIIArray("abc")

; Display the array to see that it contains the ASCII
; values for each character.
_ArrayDisplay($a)

; Now convert the array into a string.
Local $s = StringFromASCIIArray($a)

; Display the string to see that it matches the original input.
MsgBox(0, "", $s)

